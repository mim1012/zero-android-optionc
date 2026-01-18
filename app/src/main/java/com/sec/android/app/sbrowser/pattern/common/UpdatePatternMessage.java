package com.sec.android.app.sbrowser.pattern.common;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.SuCommander;
import com.sec.android.app.sbrowser.pattern.PatternMessage;
import com.sec.android.app.sbrowser.pattern.action.UpdateAction;
import com.sec.android.app.sbrowser.system.AdbController;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdatePatternMessage extends PatternMessage {

    private static final String TAG = UpdatePatternMessage.class.getSimpleName();

    private static final int CHECK_VERSION = 5000;
    private static final int RUN_DOWNLOAD = CHECK_VERSION + 1;
    private static final int INSTALL_UPDATE = CHECK_VERSION + 2;
    private static final int FINISH_UPDATE = CHECK_VERSION + 3;

    private static final int BUFFER_SIZE = 16 * 1024; // 8k ~ 32K

    protected final Context _context;
    private UpdateAction _updateAction;
    private String _logHeader;
    private boolean _updated;

    protected boolean _killAppBeforeUpdate = true;

    public UpdatePatternMessage(Context context) {
        super(null);
        _context = context;
        _updateAction = null;
        _logHeader = null;
        _updated = false;
    }

    public void setUpdateAction(UpdateAction updateAction) {
        _updateAction = updateAction;
    }

    public void setLogHeader(String logHeader) {
        _logHeader = logHeader;
    }

    public boolean isUpdated() {
        return _updated;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# " + getHeaderMessage("업데이트 시작"));
                sendMessageDelayed(CHECK_VERSION, 10);
                break;
            }

            case CHECK_VERSION: {
                Log.d(TAG, "# 버전 검사");
                clearDownloads(_context.getPackageName());
                _updateAction.getVersion(_context);

                if (_updateAction.isSuccess()) {
                    final String url = _updateAction.getUpdateUrl();

                    if (url != null) {
                        _updated = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (_killAppBeforeUpdate) {
                                    AdbController.killAppThread(_updateAction.getPackageName());
                                }

                                downloadUrl(url);
                                sendMessageDelayed(END_PATTERN, 10);
                            }
                        }).start();
                    } else {
                        Log.d(TAG, "# 이미 최신 버전이라 패턴 종료");
                        sendMessageDelayed(END_PATTERN, 10);
                    }
                } else {
                    Log.d(TAG, "# 버전 검사에 실패해서 다시 시도: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 5000, 5)) {
                        Log.d(TAG, "# 버전 검사에 실패해서 패턴 종료..");
                        sendMessageDelayed(END_PATTERN, 100);
                    }
                }
                break;
            }

            case RUN_DOWNLOAD: {
                break;
            }

            case INSTALL_UPDATE: {
                break;
            }

            case FINISH_UPDATE: {
                Log.d(TAG, "# 업데이트가 완료되어 패턴 종료");
                sendMessageDelayed(END_PATTERN, 1000);
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# " + getHeaderMessage("업데이트 종료"));
                sendEndPatternMessage();
                break;
            }

            case PAUSE_PATTERN: {
                Log.d(TAG, "# 패턴 중단");
                break;
            }
        }
    }

    public void processFile(File file) {
        String filename = file.getName();
        String extension = FilenameUtils.getExtension(filename);

        if (!TextUtils.isEmpty(extension)) {
            if (extension.equals("apk")) {
                sendStringPatternMessage(getHeaderMessage("설치 중..."));
//                ApkUtils.installAPk(this, PACKAGE_NAME, file);
                installApk(_context, file);
            } else if (extension.equals("7z")) {
                sendStringPatternMessage(getHeaderMessage("압축 해제 중..."));
                String fileName = FilenameUtils.getBaseName(filename);
                File extractDir = new File(file.getParentFile(), fileName);
                extract7zFile(file, extractDir);
                sendStringPatternMessage(getHeaderMessage("설치 중..."));
                installApkMulti(_context, extractDir.listFiles());
            } else {
                sendStringPatternMessage(getHeaderMessage("알 수 없는 파일 형식: " + extension));
            }
        } else {
            sendStringPatternMessage(getHeaderMessage("확장자 없음"));
        }
    }

    private String getHeaderMessage(String message) {
        if (_logHeader != null) {
            return _logHeader + " " + message;
        }

        return message;
    }

    private void clearDownloads(String packageName) {
        Log.d(TAG, "clear downloads.");
        String cmd = "rm -rf /data/data/" + packageName + "/cache/*.*";

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cmd = "rm -rf /data/data/" + packageName + "/cache/coupang*";

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean downloadUrl(String urlStr) {
//        NotificationHelper notificationHelper =  new NotificationHelper(this);
        InputStream in = null;
        FileOutputStream out = null;
        boolean success = false;
        try {
            sendStringPatternMessage(getHeaderMessage("다운로드 중..."));

            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            in = urlConnection.getInputStream();
            File dir = getCacheDirectory(_context);
            String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
            File downloadFile = new File(dir, fileName);
            out = new FileOutputStream(downloadFile);
            byte[] buffer = new byte[BUFFER_SIZE];

            int oldProgress = 0;

            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);

                int progress = (int) (bytesum * 100L / bytetotal);
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress) {
                    sendMessageDelayed(RUN_DOWNLOAD, 0);
                    sendStringPatternMessage(getHeaderMessage("다운로드 중..." + progress + "%"));
//                    notificationHelper.updateProgress(progress);
                }
                oldProgress = progress;
            }
            sendMessageDelayed(INSTALL_UPDATE, 0);
            processFile(downloadFile);
            success = true;

//            notificationHelper.cancel();
        } catch (Exception e) {
            Log.e(TAG, "download apk file error: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {

                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }

        return success;
    }

    private void installApk(Context context, File apkFile) {
        Uri uri;
        String path;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
            path = apkFile.getPath();
        } else {
            uri = getApkUri(apkFile);
            path = uri.getPath();
        }

        Log.d(TAG, "install " + path);
        String cmd = "/system/bin/pm install -r " + path;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void installApkMulti(Context context, File[] files) {
        File baseApkFile = null;
        List<File> fileList = new ArrayList<>();

        for (File file : files) {
            if (file.getName().endsWith("base.apk")) {
                baseApkFile = file;
            } else {
                fileList.add(file);
            }
        }

        if (baseApkFile != null) {
            installApk(context, baseApkFile, null);

            for (File file : fileList) {
                installApk(context, file, _updateAction.getPackageName());
            }
        }
    }

    private void installApk(Context context, File apkFile, String basePackageName) {
        String path;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
            path = apkFile.getPath();
        } else {
            Uri uri = getApkUri(apkFile);
            path = uri.getPath();
        }

        String cmd = "/system/bin/pm install -r ";
        String logText = "install ";

        if (!TextUtils.isEmpty(basePackageName)) {
            cmd += "-p " + basePackageName + " ";
            logText += "partial (" + basePackageName + ") ";
        }

        cmd += path;
        Log.d(TAG, logText + path);

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Uri getApkUri(File apkFile) {
        Log.d(TAG, apkFile.toString());
        //如果没有设置 SDCard 写权限，或者没有 SDCard,apk 文件保存在内存中，需要授予权限才能安装
        try {
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (IOException ignored) {
        }
        Uri uri = Uri.fromFile(apkFile);
//        Log.d(Constants.TAG, uri.toString());
        return uri;
    }

//    private void extract7z() {
////        SevenZFile.builder().setFile()
//        SevenZFile sevenZFile = new SevenZFile(new File("archive.7z"));
//        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
//        byte[] content = new byte[entry.getSize()];
//        LOOP UNTIL entry.getSize() HAS BEEN READ {
//            sevenZFile.read(content, offset, content.length - offset);
//        }
//    }

    public void extract7zFile(File inputFile, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (SevenZFile sevenZFile = new SevenZFile(inputFile)) {
            SevenZArchiveEntry entry;

            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(outputDir, entry.getName()).mkdirs();
                    continue;
                }

                File outFile = new File(outputDir, entry.getName());
                File parent = outFile.getParentFile();

                Log.d("7z", "압축 해제 중: " + outFile.getName());

                if (!parent.exists()) {
                    parent.mkdirs();
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;

                    while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    // method 1: read all.
//                    byte[] buffer = new byte[(int) entry.getSize()];
//                    sevenZFile.read(buffer, 0, buffer.length);
//                    out.write(buffer);
                }
            }

            Log.d("7z", "압축 해제 완료: " + outputDir.getAbsolutePath());
        } catch (IOException e) {
            Log.e("7z", "압축 해제 실패", e);
        }
    }

    private static File getCacheDirectory(Context context) {
        File appCacheDir = context.getCacheDir();
        if (appCacheDir == null) {
            Log.w("StorageUtils", "Can't define system cache directory! The app should be re-installed.");
        }
        return appCacheDir;
    }
}

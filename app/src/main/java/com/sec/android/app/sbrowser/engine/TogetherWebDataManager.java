package com.sec.android.app.sbrowser.engine;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TogetherWebDataManager {
//    public static final int MAX_DATA_COUNT = BuildConfig.MAX_DATA_COUNT;

    private static final String TAG = TogetherWebDataManager.class.getSimpleName();

    private static final String PREFERENCES_SETTING = "togetherWebData";

    private static final String KEY_MAX_DATA_COUNT = "maxDataCount";
    private static final String KEY_CURRENT_INDEX = "currentIndex";
    private static final String KEY_WEB_DATA_LIST = "webDataList";

    // 기본값은.. 200 개로 설정.
    private int _maxDataCount = 200;
    private int _currentIndex = 0;
    private Set<String> _stringSet = null;

    public static TogetherWebDataManager getInstance() {
        return TogetherWebDataManager.LazyHolder.INSTANCE;
    }

    public void loadData(Context context) {
        SharedPreferences pref = getPref(context);

        _maxDataCount = pref.getInt(KEY_MAX_DATA_COUNT, 200);
        _currentIndex = pref.getInt(KEY_CURRENT_INDEX, 0);
        _stringSet = pref.getStringSet(KEY_WEB_DATA_LIST, new HashSet<String>());
    }

    public void saveData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();

        editor.putInt(KEY_CURRENT_INDEX, _currentIndex);
        editor.putStringSet(KEY_WEB_DATA_LIST, _stringSet);
        editor.apply();
    }

    public void clearData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();

        editor.clear();
        editor.apply();
    }

    public int getMaxDataCount() {
        return _maxDataCount;
    }

    public void setMaxDataCount(Context context, int maxDataCount) {
        SharedPreferences.Editor editor = getPref(context).edit();

        _maxDataCount = maxDataCount;
        editor.putInt(KEY_MAX_DATA_COUNT, _maxDataCount);
        editor.apply();
    }

    public int getCurrentIndex() {
        return _currentIndex;
    }

    public Set<String> getStringSet() {
        return _stringSet;
    }


    /*
     * 기본 처리 로직.
     *
     * 1. 저장된 쿠키정보가 있는지 확인한다.
     * 2-1. 쿠기가 없다.
     * 2-2. 쿠기가 있지만 공간이 남았다.
     * 2-3. 쿠키가 꽉찼다.
     *
     *
     * # 2-1.
     * 3. 브라우저 이동이 끝날떄마다 (onFinish) 지정된 폴더에 쿠키 파일을 저장(복사)한다.
     *
     * # 2-2.
     * 3. 쿠키를 정보를 초기화한다.
     * 4. 브라우저 이동이 끝날떄마다 (onFinish) 지정된 폴더에 쿠키 파일을 저장(복사)한다.
     *
     * # 2-3.
     * 3. 쿠키를 재활용하기위해 마지막 사용했던 쿠키번호를 참고하여 다음 쿠키 파일 내용을 가져온다.
     * 4. 가져온 내용으로 쿠키 파일을 덮어쓴다.
     * 5. 브라우저 이동이 끝날떄마다 (onFinish) 지정된 폴더에 쿠키 파일을 저장(복사)한다.
     *
     */

    // 신규 쿠키 등록 모드이면.
    public boolean isNewMode() {
        return (!isFull() && (_stringSet.size() <= _currentIndex));
    }

    public boolean isEmpty() {
        return _stringSet.size() == 0;
    }

    public boolean isFull() {
        return _stringSet.size() >= _maxDataCount;
    }

    private File getSaveDataPath(int index) {
        String subDir = String.format(Locale.getDefault(), "%04d", index);
        File webDataDir = new File(getSaveDataPath(), subDir);

        if (!webDataDir.exists()) {
            if (!webDataDir.mkdirs()) {
                Log.d(TAG, "Directory not created");
            }
        }

        return webDataDir;
    }

    private File getSavedDir(Context context, int index) {
        String subDir = String.format(Locale.getDefault(), "%04d", index);
        File webDataDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "savedWebData/" + subDir);
//        File webDataDir = new File(context.getFilesDir(), "savedWebData");
        if (!webDataDir.exists()) {
            if (!webDataDir.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
        }

        return webDataDir;
    }

    private File[] getFilterdFiles(File[] files) {
        List<File> fileList = new ArrayList<>();

        for (int i = 0; i < files.length; ++i) {
            if (files[i].getName().contains("Cookie")) {
                fileList.add(files[i]);
            }
        }

        return fileList.toArray(new File[0]);
    }

    public File[] getSavedWebDataFiles(Context context, int index) {
        File dataDir = getSavedDir(context, index);
        return dataDir.listFiles();

        // 일부만 저장할때.
//        File[] files = dataDir.listFiles();
////        Log.d(TAG, "app_webview file count: " + files.length);
//        return getFilterdFiles(files);
    }


    private File getWebDataDir(Context context) {
        return new File(context.getFilesDir().getParent(), "app_webview");
    }

    public File[] getWebDataFiles(Context context) {
        File dataDir = getWebDataDir(context);
        return dataDir.listFiles();

//        File[] files = dataDir.listFiles();
////        Log.d(TAG, "app_webview file count: " + files.length);
//        return getFilterdFiles(files);
    }


    public void clearSbrowserData() {
        FileManager fm = new FileManager();
        fm.deleteSubFiles(getSbrowserPath());
    }

    public void clearAppData() {
        FileManager fm = new FileManager();
        fm.delete(getSbrowserPath()+"app_webview");
    }

    public boolean loadNextWebData(Context context) {
        // 데이터 폴더 이동.
        if (isNewMode()) {
            Log.d(TAG, "신규 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
            saveWebData(context);
        } else {
            Log.d(TAG, "덮어쓰기 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
            saveWebDataAtIndex(context, _currentIndex);
        }

        ++_currentIndex;

        boolean loaded = false;

        // 쿠키 저장 공간이 남아있다면 새로 저장하기 위해 데이터는 로드하지 않는다.
        // 즉, 가득차 있다면 실행한다.
        if (isFull()) {
            if (_currentIndex >= _maxDataCount) {
                _currentIndex = 0;
            }

//            loadWebData(context, _currentIndex);

            Log.d(TAG, "loadWebData index: " + _currentIndex + ", current total: " + _stringSet.size() + "개");
            FileManager fm = new FileManager();
            fm.delete(getSbrowserPath());
            fm.move(getSaveDataPath(_currentIndex).getPath(), getSbrowserPath());

            loaded = true;
        }

        saveData(context);

        return loaded;
    }

    private boolean deleteRecursive(File targetFile) {
        boolean success = true;

        if (targetFile.isDirectory()) {
            for (File child : targetFile.listFiles()) {
                success &= deleteRecursive(child);
            }
        }

        success = targetFile.delete();

        if (!success) {
            Log.d(TAG, "File delete falied: " + targetFile.getAbsoluteFile());
        }

        return success;
    }
    private String getAppCachePath() {
        return "/data/data/com.shop.up/cache";
    }
    private String getAppdataPath() {
        return "/data/data/com.shop.up/app_webview";
    }

    private String getSbrowserPath() {
        return "/data/data/com.sec.android.app.sbrowser/";
    }

    private String getSaveDataPath() {
        return "/data/asura";
    }

    private void loadWebData(Context context, int index) {
        Log.d(TAG, "loadWebData index: " + index + ", cur rent total: " + _stringSet.size() + "개");

        File webDataDir = getWebDataDir(context);
        File[] oriFiles = webDataDir.listFiles();

        // 먼저 지우고 로드한다.
        for (File file : oriFiles) {
            deleteRecursive(file);
        }

        File[] files = getSavedWebDataFiles(context, index);

        boolean success = copy(files, webDataDir);


//        File cookieFile = getCookieFile(context);
//
//        if (!cookieFile.exists()) {
//            Log.d(TAG, "Cookie file is not exist.");
//            return;
//        }
//
//        Log.d(TAG, "original: " + cookieFile.getName() + "(" + cookieFile.length() + "byte)");
//
//        File savedCookieDir = getSavedDir(context);
//        String fileName = String.format(Locale.getDefault(), "%04d_Cookies", _currentIndex);
//        File saveFile = new File(savedCookieDir, fileName);
//
//        if (!saveFile.exists()) {
//            Log.d(TAG, "Saved cookie file is not exist.");
//            return;
//        }
//
//        try {
//            copyFile(saveFile, cookieFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.d(TAG, "load after: " + cookieFile.getName() + "(" + cookieFile.length() + "byte)");
    }


    /*
    쿠키 관리.
    1. 쿠키가 없다.
    -> 저장한다. 다음 인덱스로 이동하고 종료한다.
    -> 즉 인덱스 이동은 종료할때만 한다.
    2. 쿠키가 하나 있다. 최대 5개다.
    ->






     */

    public void saveCurrentWebData(Context context) {
        return;
//        if (_currentIndex < 0) {
//            Log.d(TAG, "최초 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//            saveWebData(context);
//        } else if (_currentIndex >= _stringSet.size()) {
//            if (!isFull()) {
//                Log.d(TAG, "신규 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//                saveWebData(context);
//            } else {
//                Log.d(TAG, "덮어쓰기 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//                saveWebDataAtIndex(context, _currentIndex);
//            }
//        }

//        if (isNewMode()) {
//            Log.d(TAG, "신규 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//            saveWebData(context);
//        } else {
//            Log.d(TAG, "덮어쓰기 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//            saveWebDataAtIndex(context, _currentIndex);
//        }

//        // 처음 저장이거나.
//        if ((_currentIndex < 0) ||
//                (_currentIndex == _stringSet.size()) ||
//                _stringSet.size() == 0) {
//
//        }
//
//        if ((_currentIndex < 0) || _stringSet.size() == 0) {
//            saveWebData(context);
//        } else {
//            saveWebDataAtIndex(context, _currentIndex);
//        }
    }

    // 인덱스의 쿠키에 덮어쓰기.
    private void saveWebDataAtIndex(Context context, int index) {
        if ((index < 0) || (index >= _stringSet.size())) {
            Log.d(TAG, "Invalid index.");
            return;
        }

        FileManager fm = new FileManager();
        fm.delete(getSaveDataPath(index).getPath());

//        fm.copySubFiles(getSbrowserPath(), getSavedDir(context, index).getPath());
        fm.move(getSbrowserPath(), getSaveDataPath(index).getPath());

        _currentIndex = index;
        saveData(context);
        Log.d(TAG, "move success");

//        File savedWebDataDir = getSavedDir(context, index);
//        File[] files = getWebDataFiles(context);
//
//        boolean success = copy(files, savedWebDataDir);
//
//        if (success) {
//            _currentIndex = index;
//            saveData(context);
//            Log.d(TAG, "copy success");
//        } else {
//            Log.d(TAG, "copy failed");
//        }
    }

    // 새로운 파일에 추가 저장.
    private void saveWebData(Context context) {
        if (isFull()) {
            Log.d(TAG, "Data is full.");
            return;
        }

        int nextIndex = _stringSet.size();

        FileManager fm = new FileManager();
        fm.delete(getSaveDataPath(nextIndex).getPath());

//        fm.copySubFiles(getSbrowserPath(), getSavedDir(context, nextIndex).getPath());
        fm.move(getSbrowserPath(), getSaveDataPath(nextIndex).getPath());

        _currentIndex = nextIndex;
        // 복사가 성공하면 파일명을 기록해둔다.
        _stringSet.add(Integer.toString(nextIndex));
        saveData(context);
        Log.d(TAG, "move success");

//        File savedWebDataDir = getSavedDir(context, nextIndex);
//        File[] files = getWebDataFiles(context);
//
//        boolean success = copy(files, savedWebDataDir);
//
//        if (success) {
//            _currentIndex = nextIndex;
//            // 복사가 성공하면 파일명을 기록해둔다.
//            _stringSet.add(Integer.toString(nextIndex));
//            saveData(context);
//            Log.d(TAG, "copy success");
//        } else {
//            Log.d(TAG, "copy failed");
//        }

//        Log.d(TAG, "original: " + cookieFile.getName() + "(" + cookieFile.length() + "byte)");
    }

    public void logSavedFiles(Context context) {
        File savedCookieDir = getSavedDir(context, _currentIndex);
        File[] files = savedCookieDir.listFiles();
        Log.d(TAG, "saved file count: " + files.length);
        for (int i = 0; i < files.length; ++i) {
            Log.d(TAG, "file" + i + ": " + files[i].getName() + "(" + files[i].length() + "byte)");
        }
    }

    public void logSavedFiles2(Context context) {
        if (true) {
            return;
        }

//        File savedCookieDir = new File(context.getFilesDir().getParent(), "app_webview");
//        File[] files = savedCookieDir.listFiles();
        File[] files = getWebDataFiles(context);
        Log.d(TAG, "app_webview file count: " + files.length);
        for (int i = 0; i < files.length; ++i) {
            Log.d(TAG, "file" + i + ": " + files[i].getName() + "(" + files[i].length() + "byte)");
        }
    }

    private boolean copy(File[] files, File dst) {
        boolean success = true;

        for (File file : files) {
            File saveFile = new File(dst, file.getName());

            try {
                copyDirectory(file, saveFile);
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }


    public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public void copyDirectory(File sourceLocation ,File targetLocation) throws IOException {
        // 레벨디비는 제외시킨다.
        if (sourceLocation.getName().contains("leveldb")) {
            Log.d(TAG, "copy skip leveldb");
            return;
        }

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            if (!targetLocation.exists()) {
                targetLocation.createNewFile();
            }

            copyFile(sourceLocation, targetLocation);
        }
    }

    // using channel
    private void fileCopyUsingNIOChannelClass(File src, File dst) throws IOException {
        FileInputStream inputStream = new FileInputStream(src);
        FileOutputStream outputStream = new FileOutputStream(dst);

        FileChannel inChannel = inputStream.getChannel();
        FileChannel outChannel = outputStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        inputStream.close();
        outputStream.close();
    }

    public void save() {
        /*
        쿠키 파일을 가져온다.
        다른 이름으로 저장한다.
        현재 지정된 이름이 있다면 그것으로 저장한다.
         */


        CookieManager manager = CookieManager.getInstance();
        manager.flush();

    }


    private static class LazyHolder {
        public static final TogetherWebDataManager INSTANCE = new TogetherWebDataManager();
    }

    private TogetherWebDataManager() {
    }

    private SharedPreferences getPref(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_SETTING, Context.MODE_PRIVATE);
    }

}

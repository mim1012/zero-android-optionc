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

public class TogetherCookieManager {
//    public static final int MAX_DATA_COUNT = 3;
    public static final int MAX_COOKIES = 200;

    private static final String TAG = "TogetherCookieManager";

    private static final String PREFERENCES_SETTING = "togetherData";

    private static final String KEY_CURRENT_INDEX = "currentIndex";
    private static final String KEY_COOKIE_LIST = "cookieList";

    public int testValue = 100;

    private boolean _alarmEnabled = true;
    private boolean _alarmSoundEnabled = true;
    private boolean _alarmVibrateEnabled = true;
    private boolean _sendForReturnKeyEnabled = false;

    private int _currentIndex = 0;
    private Set<String> _stringSet = null;

    public static TogetherCookieManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void loadData(Context context) {
        SharedPreferences pref = getPref(context);

        _currentIndex = pref.getInt(KEY_CURRENT_INDEX, 0);
        _stringSet = pref.getStringSet(KEY_COOKIE_LIST, new HashSet<String>());
    }

    public void saveData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();

        editor.putInt(KEY_CURRENT_INDEX, _currentIndex);
        editor.putStringSet(KEY_COOKIE_LIST, _stringSet);
        editor.apply();
    }

    public void clearData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();

        editor.clear();
        editor.apply();
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
    public boolean isNewCookieMode() {
        return (!isFull() && (_stringSet.size() <= _currentIndex));
    }

    public boolean isEmpty() {
        return _stringSet.size() == 0;
    }

    public boolean isFull() {
        return _stringSet.size() == MAX_COOKIES;
    }

    private File getSavedDir(Context context, int index) {
        String subDir = String.format(Locale.getDefault(), "%04d", index);
        File cookieDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "savedCookies/" + subDir);
//        File cookieDir = new File(context.getFilesDir(), "savedCookies");
        if (!cookieDir.exists()) {
            if (!cookieDir.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
        }

        return cookieDir;
    }

    private File getSavedDir(Context context) {
        File cookieDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "savedCookies");
//        File cookieDir = new File(context.getFilesDir(), "savedCookies");
        if (!cookieDir.exists()) {
            if (!cookieDir.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
        }

        return cookieDir;
    }

    public File[] getSavedCookieFiles(Context context, int index) {
        File cookieDir = getSavedDir(context, index);
        File[] files = cookieDir.listFiles();
//        Log.d(TAG, "app_webview file count: " + files.length);

        List<File> fileList = new ArrayList<>();

        for (int i = 0; i < files.length; ++i) {
            if (files[i].getName().contains("Cookie")) {
                fileList.add(files[i]);
            }
        }

        return fileList.toArray(new File[0]);
    }


    private File getCookieDir(Context context) {
        return new File(context.getFilesDir().getParent(), "app_webview");
    }

    private File getCookieFile(Context context) {
        // get cookie file in Android api 17
        //File cokieFile = new   File(getFilesDir().getParent()+"/app_webview/Cookies");

        // get cookie file in Android api 23
        //File webviewCookiesChromiumFile = new File(getFilesDir().getParent()+"/databases/webviewCookiesChromium.db");

        return new File(context.getFilesDir().getParent(), "app_webview/Cookies");
    }

    public File[] getCookieFiles(Context context) {
        File cookieDir = getCookieDir(context);
        File[] files = cookieDir.listFiles();
//        Log.d(TAG, "app_webview file count: " + files.length);

        List<File> fileList = new ArrayList<>();

        for (int i = 0; i < files.length; ++i) {
            if (files[i].getName().contains("Cookie")) {
                fileList.add(files[i]);
            }
        }

        return fileList.toArray(new File[0]);
    }



    public boolean loadNextCookie(Context context) {
        ++_currentIndex;

        boolean loaded = false;

        // 쿠키 저장 공간이 남아있다면 새로 저장하기 위해 데이터는 로드하지 않는다.
        // 즉, 가득차 있다면 실행한다.
        if (isFull()) {
            if (_currentIndex >= _stringSet.size()) {
                _currentIndex = 0;
            }

            loadCookie(context, _currentIndex);
            loaded = true;
        }

        saveData(context);

        return loaded;
    }

    private void loadCookie(Context context, int index) {
        Log.d(TAG, "loadCookie: " + index + ", current total: " + _stringSet.size());

        File cookieDir = getCookieDir(context);
        File[] files = getSavedCookieFiles(context, index);

        boolean success = true;

        for (File file : files) {
            File saveFile = new File(cookieDir, file.getName());

            try {
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }

                copyFile(file, saveFile);

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }


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

    public void saveCurrentCookie(Context context) {
//        if (_currentIndex < 0) {
//            Log.d(TAG, "최초 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//            saveCookie(context);
//        } else if (_currentIndex >= _stringSet.size()) {
//            if (!isFull()) {
//                Log.d(TAG, "신규 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//                saveCookie(context);
//            } else {
//                Log.d(TAG, "덮어쓰기 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
//                saveCookieAtIndex(context, _currentIndex);
//            }
//        }

        if (isNewCookieMode()) {
            Log.d(TAG, "신규 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
            saveCookie(context);
        } else {
            Log.d(TAG, "덮어쓰기 저장: 현재 인덱스 " + _currentIndex + "/ 저장전 " + _stringSet.size() + "개");
            saveCookieAtIndex(context, _currentIndex);
        }

//        // 처음 저장이거나.
//        if ((_currentIndex < 0) ||
//                (_currentIndex == _stringSet.size()) ||
//                _stringSet.size() == 0) {
//
//        }
//
//        if ((_currentIndex < 0) || _stringSet.size() == 0) {
//            saveCookie(context);
//        } else {
//            saveCookieAtIndex(context, _currentIndex);
//        }
    }

    // 인덱스의 쿠키에 덮어쓰기.
    private void saveCookieAtIndex(Context context, int index) {
        File cookieFile = getCookieFile(context);

        if (!cookieFile.exists()) {
            Log.d(TAG, "Cookie file is not exist.");
            return;
        }

        if ((index < 0) || (index >= _stringSet.size())) {
            Log.d(TAG, "Invalid index.");
            return;
        }

        File savedCookieDir = getSavedDir(context, index);
        File[] files = getCookieFiles(context);

        boolean success = true;

        for (File file : files) {
            File saveFile = new File(savedCookieDir, file.getName());

            try {
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }

                copyFile(file, saveFile);

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            _currentIndex = index;
            saveData(context);
            Log.d(TAG, "copy success");
        } else {
            Log.d(TAG, "copy failed");
        }
    }

    // 새로운 파일에 추가 저장.
    private void saveCookie(Context context) {
        File cookieFile = getCookieFile(context);

        if (!cookieFile.exists()) {
            Log.d(TAG, "Cookie file is not exist.");
            return;
        }

        if (_stringSet.size() >= MAX_COOKIES) {
            Log.d(TAG, "Cookie is full.");
            return;
        }

        int nextIndex = _stringSet.size();
        File savedCookieDir = getSavedDir(context, nextIndex);
        File[] files = getCookieFiles(context);

        boolean success = true;

        for (File file : files) {
            File saveFile = new File(savedCookieDir, file.getName());

            try {
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }

                copyFile(file, saveFile);

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            _currentIndex = nextIndex;
            // 복사가 성공하면 파일명을 기록해둔다.
            _stringSet.add(Integer.toString(nextIndex));
            saveData(context);
            Log.d(TAG, "copy success");
        } else {
            Log.d(TAG, "copy failed");
        }

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
//        File savedCookieDir = new File(context.getFilesDir().getParent(), "app_webview");
//        File[] files = savedCookieDir.listFiles();
        File[] files = getCookieFiles(context);
        Log.d(TAG, "app_webview file count: " + files.length);
        for (int i = 0; i < files.length; ++i) {
            Log.d(TAG, "file" + i + ": " + files[i].getName() + "(" + files[i].length() + "byte)");
        }
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

//    public boolean isAlarmEnabled() {
//        return _alarmEnabled;
//    }
//
//    public void setAlarmEnabled(boolean enabled) {
//        _alarmEnabled = enabled;
//    }
//
//    public boolean isAlarmSoundEnabled() {
//        return _alarmSoundEnabled;
//    }
//
//    public void setAlarmSoundEnabled(boolean enabled) {
//        _alarmSoundEnabled = enabled;
//    }
//
//    public boolean isAlarmVibrateEnabled() {
//        return _alarmVibrateEnabled;
//    }
//
//    public void setAlarmVibrateEnabled(boolean enabled) {
//        _alarmVibrateEnabled = enabled;
//    }
//
//    public boolean isSendForReturnKeyEnabled() {
//        return _sendForReturnKeyEnabled;
//    }
//
//    public void setSendForReturnKeyEnabled(boolean enabled) {
//        _sendForReturnKeyEnabled = enabled;
//    }


    private static class LazyHolder {
        public static final TogetherCookieManager INSTANCE = new TogetherCookieManager();
    }

    private TogetherCookieManager() {
    }

    private SharedPreferences getPref(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_SETTING, Context.MODE_PRIVATE);
    }
}

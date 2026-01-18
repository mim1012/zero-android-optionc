package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class CookieFileManager {

    private static final String TAG = CookieFileManager.class.getSimpleName();
    private static final String NEW_KEY = "New";

    public static final int BROWSER_CHROME = 0;
    public static final int BROWSER_SAMSUNG = 1;
    public static final int BROWSER_NAVER = 2;
    public static final int BROWSER_FIREFOX = 3;

    private Map<String, String> _values;

    private int _browserType = BROWSER_SAMSUNG;

    public CookieFileManager() {
        _values = new HashMap<>();

        // Test for bytesSearch.
//        byte[] outer = {1, 2, 3, 4};
//
//        int b4 = bytesSearch(outer, new byte[] {4, 5});
//        int b3 = bytesSearch(outer, new byte[] {3, 4});
//        int b1 = bytesSearch(outer, new byte[] {5, 6});
//        int b2 = bytesSearch(outer, new byte[] {1, 2});
//        int b5 = bytesSearch(outer, new byte[] {1, 2, 4});
//        int b6 = bytesSearch(outer, new byte[] {1, 2, 3, 4, 5});
    }

    public int getBrowserType() {
        return _browserType;
    }

    public void setBrowserType(int browserType) {
        _browserType = browserType;
    }

    public String getNewValue(String key) {
        return _values.get(key + NEW_KEY);
    }

    public String getValue(String key) {
        return _values.get(key);
    }

    public void deleteCookie() {
        String originPath = getCookieFilepath();
        FileManager fm = new FileManager();
        fm.delete(originPath);
    }

    public boolean changeCookieValue(String key, String value) {
        String originPath = getCookieFilepath();
        String targetPath = getSaveCookieFilepath();
        FileManager fm = new FileManager();
        fm.delete(targetPath);
        fm.copy(originPath, targetPath);

        int result = changeValue(targetPath, key, value);

        if (result != 1) {
            Log.d(TAG, "Cookie value change failed: " + result);
            return false;
        }

        fm.copy(targetPath, originPath);
        fm.delete(targetPath);
        Log.d(TAG, "Cookie value change success");

        return true;
    }

    // 퍼포먼스가 떨어짐.. 최적화 필요.
    public boolean changeCookieValues(@NonNull Map<String, String> values) {
        if (values.size() <= 0) {
            Log.d(TAG, "empty values");
            return false;
        }

        String originPath = getCookieFilepath();
        String targetPath = getSaveCookieFilepath();
        FileManager fm = new FileManager();
        fm.delete(targetPath);
        fm.copy(originPath, targetPath);

        int result = 1;

        for (String key : values.keySet()) {
            result = changeValue(targetPath, key, values.get(key));
            if (result != 1) {
                break;
            }
        }

        if (result != 1) {
            Log.d(TAG, "Cookie values change failed: " + result);
            return false;
        }

        fm.copy(targetPath, originPath);
        fm.delete(targetPath);
        Log.d(TAG, "Cookie values change success");

        return true;
    }

    public String getCookieValue(Context context, String key) {
        String originPath = getCookieFilepath();
        String targetPath;
        String result = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            targetPath = getSaveCookieFilepath() + "1";
        } else {
            targetPath = getSaveCookieFilepath(context) + "1";
        }

        try {
//            File originDir = new File(originPath);
//            if (!originDir.exists()) {
//                return null;
//            }

            FileManager fm = new FileManager();
            fm.delete(targetPath);
            fm.copy(originPath, targetPath);

            switch (_browserType) {
                case BROWSER_CHROME:
                    result = getValueDecrypt(targetPath, key);
                    break;

                default:
                    result = getValue(targetPath, key);
                    break;
            }

            fm.delete(targetPath);
        } catch (Exception e) {
            Log.d(TAG, "" + e.getLocalizedMessage());
        }

        return result;
    }

    public String getAllCookieString(Context context, String key) {
        String originPath = getCookieFilepath();
        String targetPath;
        String result = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            targetPath = getSaveCookieFilepath() + "1";
        } else {
            targetPath = getSaveCookieFilepath(context) + "1";
        }

        try {
            File originDir = new File(originPath);
            if (!originDir.exists()) {
                return null;
            }

            FileManager fm = new FileManager();
            fm.delete(targetPath);
            fm.copy(originPath, targetPath);

            result = getAllCookieForSet(targetPath, key);

            fm.delete(targetPath);
        } catch (Exception e) {
            Log.d(TAG, "" + e.getLocalizedMessage());
        }

        return result;
    }


    private String getCookieFilepath() {
        switch (_browserType) {
            case BROWSER_CHROME:
                return "/data/data/com.android.chrome/app_chrome/Default/Cookies";

            default:
                // 삼성브라우저.
                return "/data/data/com.sec.android.app.sbrowser/app_webview/Default/Cookies";
        }
    }

    private String getSaveCookieFilepath() {
        return Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getPath() + "/Cookies";
    }

    private String getSaveCookieFilepath(Context context) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        try {
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.d(TAG, "Directory not created");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "" + e.getLocalizedMessage());
        }

        return dir.getPath() + "/Cookies";
    }


    private String getAllCookieForSet(String path, String domain) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        String str = null;

        String[] selectionArgs = {"%" + domain};
        Cursor cursor = db.rawQuery("SELECT `host_key`, `name`, `value`, `path`, `expires_utc`, `is_secure`, `is_httponly`, `samesite` FROM `cookies` WHERE `host_key` LIKE ?", selectionArgs);
        List<String> cookieList = new LinkedList<>();

        while (cursor.moveToNext()) {
            String cDomain = cursor.getString(0);
            String cName = cursor.getString(1);
            String cValue = cursor.getString(2);
            String cPath = cursor.getString(3);
            long cExpires = cursor.getLong(4);
            int cSecure = cursor.getInt(5);
            int cHttpOnly = cursor.getInt(6);
            int cSameSite = cursor.getInt(7);

            if (cName.toUpperCase().startsWith("NNB")) {
                continue;
            }

            StringBuilder cookieString = new StringBuilder();
            cookieString.append(cName + "=" + cValue);

            //path, domain, max-age, expires, secure, samesite

            if (cExpires > 0) {
                long timeStamp = cExpires - 11644473600000000L;
                Date expire = new Date(timeStamp / 1000);

//                SimpleDateFormat rfc1123Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                SimpleDateFormat rfc1123Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
//                rfc1123Format.setTimeZone(TimeZone.getTimeZone("UTC"));

//                simpleDateFormat.
//                DateFormat dateFormat = DateFormat.getDateTimeInstance()
                String expireString = rfc1123Format.format(expire);
                cookieString.append("; expires=").append(expireString);
            }

            if (!TextUtils.isEmpty(cPath)) {
                cookieString.append("; path=").append(cPath);
            }

            if (!TextUtils.isEmpty(cDomain)) {
                cookieString.append("; domain=").append(cDomain);
            }

            if (cSecure == 1) {
                cookieString.append("; Secure");
            }

            if (cSameSite == 0) {
                cookieString.append("; SameSite=").append("None");
            }

            if (cHttpOnly == 1) {
                cookieString.append("; HttpOnly");
            }

            cookieList.add(cookieString.toString());
        }

        cursor.close();

        if (db.isOpen()) {
            db.close();
        }

        if (!cookieList.isEmpty()) {
            str = String.join("\n", cookieList);
        }

        return str;
    }

    private String getValue(String path, String key) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        String[] selectionArgs = {key};
        Cursor resultSet = db.rawQuery("SELECT `value` FROM `cookies` WHERE `name` LIKE ?", selectionArgs);

        if (!resultSet.moveToFirst()) {
            resultSet.close();

            if (db.isOpen()) {
                db.close();
            }

            return null;
        }

//        int type = resultSet.getType(0);
//        resultSet.getColumnIndexOrThrow()

        String value = resultSet.getString(0);
        resultSet.close();

        if (db.isOpen()) {
            db.close();
        }

        return value;
    }

    private String getValueDecrypt(String path, String key) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        String[] selectionArgs = {key};
        Cursor resultSet = db.rawQuery("SELECT `encrypted_value` FROM `cookies` WHERE `name` LIKE ?", selectionArgs);

        if (!resultSet.moveToFirst()) {
            resultSet.close();

            if (db.isOpen()) {
                db.close();
            }

            return null;
        }

//        int type = resultSet.getType(0);
//        resultSet.getColumnIndexOrThrow()

        byte[] encryptedValue = resultSet.getBlob(0);
        resultSet.close();

        if (db.isOpen()) {
            db.close();
        }

        byte[] version = Arrays.copyOfRange(encryptedValue, 0, 3);
        String v = new String(version);
        Log.d(TAG, "+ obfuscation version: " + v);
        byte[] slicedValue = Arrays.copyOfRange(encryptedValue, 3, encryptedValue.length);
//        byte[] nonce = Arrays.copyOfRange(encryptedValue, 3, 15);
//        byte[] ciphertext = Arrays.copyOfRange(encryptedValue, 15, (encryptedValue.length - 16));
//        byte[] tag = Arrays.copyOfRange(encryptedValue, (encryptedValue.length - 16), encryptedValue.length);

        return decryptValue(slicedValue);
    }

    private String decryptValue(byte[] encryptedValue) {
        try {
            SecretKey secretKey = generateKey();
            IvParameterSpec iv = getIv();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decrypted = cipher.doFinal(encryptedValue);
            byte[] randHead = Arrays.copyOfRange(decrypted, 0, 32);
            String rh = Base64.encodeToString(randHead, Base64.NO_WRAP);
            Log.d(TAG, "+ skipped obfuscation head: " + rh);
            byte[] data = Arrays.copyOfRange(decrypted, 32, decrypted.length);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }

        return "";
    }

    private SecretKey generateKey() throws Exception {
        String passwordString = "peanuts";
        String saltString = "saltysalt";

        char[] password = passwordString.toCharArray();
        byte[] salt = saltString.getBytes();
        int iterations = 1;
//        int iterations = 1003;  // for mac
        int keyLengthBits = 128;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        return secretKeyFactory.generateSecret(keySpec);
    }

    private IvParameterSpec getIv() {
        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) ' ');
        return new IvParameterSpec(iv);
    }

    /**
     * @param path
     * @param key
     * @param newValue
     * @return 1: success, 0: failed, -1: file not found, -2: key not found.
     */
    private int changeValue(String path, String key, String newValue) {
        File file = new File(path);

        if (!file.exists()) {
            return -1;
        }

        String value = getValue(path, key);

        if (value == null) {
            return -2;
        }

        _values.put(key, value);
        _values.put(key + NEW_KEY, newValue);

        Log.d(TAG, "prev " + key + ": " + value);

        byte[] fileBytes = FileManager.getBytesFromFile(file);
        byte[] valueBytes = value.getBytes();
        int findIndex = bytesSearch(fileBytes, valueBytes);

        if (findIndex > -1) {
            System.arraycopy(newValue.getBytes(), 0, fileBytes, findIndex, valueBytes.length);

            if (writeFile(file, fileBytes)) {
                return 1;
            }
        }

        return 0;
    }

    private int bytesSearch(byte[] baseArray, byte[] searchArray) {
        int needleLength = searchArray.length;
        int length = baseArray.length - needleLength + 1;

        for (int i = 0; i < length; ++i) {
            boolean found = true;

            for (int j = 0; j < needleLength; ++j) {
                if (baseArray[i + j] != searchArray[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return i;
            }
        }

        return -1;
    }

    private boolean writeFile(File file, byte[] bytes) {
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.flush();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}

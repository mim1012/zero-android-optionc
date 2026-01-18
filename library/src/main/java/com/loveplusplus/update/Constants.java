package com.loveplusplus.update;


class Constants {


    // json {"url":"http://192.168.205.33:8080/Hello/app_v3.0.1_Other_20150116.apk","versionCode":2,"updateMessage":"版本更新信息"}

    static final String APK_DOWNLOAD_URL = "url";
    static final String APK_UPDATE_CONTENT = "update_message";
    static final String APK_VERSION_CODE = "version_code";


    static final int TYPE_DIALOG = 1;
    static final int TYPE_NOTIFICATION = 2;
    static final int TYPE_BACKGROUND = 3;

    static final String TAG = "UpdateChecker";
//  static final String UPDATE_URL = "http://167.179.69.84/main/setting/get.php?parent=T";
//  static final String UPDATE_URL = "http://167.179.69.84/main/setting/get.php?parent=F";
  //  static final String UPDATE_URL = "http://198.13.48.146/main/setting/get.php?parent=T";
     static final String UPDATE_URL = "http://198.13.48.146/main/setting/webview_get.php";
}

# Option C: 서버 기반 동적 설정 통합 가이드

이 문서는 원본 Zero 앱에 **Option C (서버 기반 동적 설정)** 기능을 통합하는 방법을 안내합니다.

**목표**: APK 재배포 없이 서버에서 WebView 설정, 헤더, User-Agent 등을 동적으로 제어합니다.

**참고 자료**:
- **통합 예제 코드**: `app/src/main/java/com/sec/android/app/sbrowser/server/INTEGRATION_EXAMPLE.java`
- **서버 리포지토리**: [mim1012/zero-ser-ver](https://github.com/mim1012/zero-ser-ver)

---

## 1단계: 사전 준비

### FastAPI 서버 배포

가장 먼저, 설정을 제공할 FastAPI 서버를 배포해야 합니다. 서버 배포 방법은 아래 문서를 참고하세요.

> **[서버 배포 가이드 (OPTION_C_DEPLOYMENT_GUIDE.md)](https://github.com/mim1012/zero-ser-ver/blob/main/OPTION_C_DEPLOYMENT_GUIDE.md)**

서버 배포가 완료되면, Android 앱에서 사용할 **서버 URL**을 확보해야 합니다. (예: `https://my-zero-server.railway.app`)

### Android 프로젝트 확인

이 리포지토리에는 Option C 관련 코드가 이미 포함되어 있습니다. `app/src/main/java/com/sec/android/app/sbrowser/server/` 디렉토리에서 다음 파일들을 확인할 수 있습니다:

- `ConfigManager.java`: 서버와 통신하여 설정을 가져오고 캐시합니다.
- `CustomWebViewClient.java`: 서버 설정을 WebView에 적용합니다.
- `WebViewHelper.java`: WebView 초기화를 돕습니다.
- `INTEGRATION_EXAMPLE.java`: 실제 통합 방법을 보여주는 예제입니다.

---

## 2단계: 기존 Activity 코드 수정

이제 기존 Zero 앱의 WebView를 사용하는 Activity (예: `ActivityMCloud.java`)에 서버 설정 로직을 추가합니다.

### 2.1. 멤버 변수 추가

Activity 클래스에 서버 URL, `ConfigManager`, `WebView` 변수를 선언합니다.

```java
// import com.sec.android.app.sbrowser.server.ConfigManager;
// import com.sec.android.app.sbrowser.server.WebViewHelper;
// import com.sec.android.app.sbrowser.server.CustomWebViewClient;

public class ActivityMCloud extends AppCompatActivity {
    
    // 1. 실제 배포된 서버 URL로 변경하세요.
    private static final String SERVER_URL = "https://zero-ser-ver-production.up.railway.app"; 
    
    private WebView webView;
    private ConfigManager configManager;
    
    // ... 기존 코드
}
```

### 2.2. `onCreate()` 메소드 수정

`onCreate()` 메소드에서 `ConfigManager`를 초기화하고, 서버에서 설정을 비동기적으로 가져온 후 WebView를 초기화하도록 코드를 수정합니다.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mcloud);
    
    // 2. ConfigManager 인스턴스 가져오기
    configManager = ConfigManager.getInstance(this, SERVER_URL);
    
    // 3. 기기 정보 설정
    String deviceModel = Build.MODEL; 
    String chromeVersion = "143"; // 앱에서 사용하는 WebView 버전에 맞게 설정
    
    // 4. 백그라운드 스레드에서 서버 설정 다운로드
    new Thread(() -> {
        // 서버에서 최신 설정을 동기적으로 다운로드 시도
        boolean success = configManager.updateFromServer(deviceModel, chromeVersion);
        
        if (success) {
            Log.i("ActivityMCloud", "서버에서 설정을 성공적으로 가져왔습니다.");
        } else {
            Log.w("ActivityMCloud", "서버 연결 실패. 캐시된 설정으로 WebView를 초기화합니다.");
        }
        
        // 5. UI 스레드에서 WebView 초기화
        // 성공 여부와 관계없이 캐시된 설정 또는 기본값으로 초기화 진행
        runOnUiThread(() -> {
            initializeWebViewAndLoadUrl();
        });
        
    }).start();
    
    // ... 기존 onCreate 코드
}
```

### 2.3. WebView 초기화 및 URL 로드 메소드 생성

`WebViewHelper`와 `CustomWebViewClient`를 사용하여 WebView를 설정하고, 서버에서 받은 헤더와 함께 URL을 로드하는 메소드를 만듭니다.

```java
private void initializeWebViewAndLoadUrl() {
    // 6. WebView 인스턴스 찾기
    webView = findViewById(R.id.webView); // 레이아웃의 WebView ID에 맞게 수정
    
    // 7. WebViewHelper를 사용하여 서버 설정 적용
    // (User-Agent, JavaScript, Cache 등 모든 설정이 여기서 적용됩니다)
    WebViewHelper.initializeWebView(this, webView, configManager);
    
    // 8. CustomWebViewClient를 사용하여 서버 헤더와 함께 URL 로드
    CustomWebViewClient client = (CustomWebViewClient) webView.getWebViewClient();
    
    // 로드할 URL (예시)
    String targetUrl = "https://m.shopping.naver.com/";
    client.loadUrlWithServerHeaders(webView, targetUrl);
    
    Log.d("ActivityMCloud", "WebView가 서버 설정으로 초기화되었고 URL 로드를 시작합니다: " + targetUrl);
}
```

### 2.4. `onResume()` 메소드 수정 (선택 사항)

앱이 다시 활성화될 때마다 주기적으로 설정을 자동 업데이트하려면 `onResume()`에 아래 코드를 추가합니다.

```java
@Override
protected void onResume() {
    super.onResume();
    
    // 9. 1시간마다 설정 자동 업데이트
    if (configManager != null) {
        String deviceModel = Build.MODEL;
        String chromeVersion = "143";
        
        new Thread(() -> {
            configManager.autoUpdateIfNeeded(deviceModel, chromeVersion);
        }).start();
    }
    
    // ... 기존 onResume 코드
}
```

---

## 3단계: `build.gradle` 및 `AndroidManifest.xml` 확인

### `build.gradle`

`ConfigManager`가 사용하는 **OkHttp** 라이브러리가 `app/build.gradle` 파일에 포함되어 있는지 확인합니다. 이 프로젝트에는 이미 추가되어 있습니다.

```groovy
// app/build.gradle
dependencies {
    // ...
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    // ...
}
```

### `AndroidManifest.xml`

서버와 통신하려면 인터넷 권한이 필요합니다. `app/src/main/AndroidManifest.xml`에 아래 권한이 있는지 확인하세요.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 4단계: 빌드 및 테스트

1.  Android Studio에서 프로젝트를 열고 **Build > Make Project**를 실행하여 컴파일 오류가 없는지 확인합니다.
2.  앱을 실행하고 Logcat에서 `ConfigManager`, `WebViewHelper`, `ActivityMCloud` 등의 태그로 로그를 필터링하여 서버 설정이 정상적으로 적용되는지 확인합니다.
    -   서버 연결 성공 시: `Config updated from server successfully`
    -   User-Agent 설정 로그: `User-Agent set: ...`
    -   URL 로드 로그: `Loading URL with custom headers: ...`
3.  서버의 `config` 파일을 수정한 후, 앱을 재시작했을 때 변경된 설정(User-Agent, 헤더 등)이 적용되는지 확인합니다.

이제 개발팀은 이 가이드를 참고하여 원본 Zero 앱의 필요한 부분에 서버 기반 동적 설정 기능을 선택적으로 통합할 수 있습니다.

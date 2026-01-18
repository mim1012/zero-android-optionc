# Zero Android App - Option C Integration

이 프로젝트는 원본 Zero Android 앱에 **Option C (서버 기반 동적 설정 관리)** 기능을 추가한 버전입니다.

## 📋 개요

**Option C**는 APK를 재배포하지 않고도 서버에서 설정을 동적으로 변경할 수 있는 시스템입니다. 1500대 이상의 Android 기기를 효율적으로 관리할 수 있도록 설계되었습니다.

### 주요 기능

- ✅ **서버 기반 설정 관리**: JSON 파일로 헤더, User-Agent, WebView 설정 관리
- ✅ **자동 업데이트**: 1시간마다 서버에서 최신 설정 자동 다운로드
- ✅ **오프라인 캐시**: 서버 연결 실패 시 로컬 캐시 사용
- ✅ **대장봇/쫄병봇 역할 관리**: 서버에서 기기별 역할 동적 할당
- ✅ **WebView 업데이트 관리**: 기기 모델별 WebView 버전 제어

## 📁 프로젝트 구조

```
app/src/main/java/com/sec/android/app/sbrowser/
├── server/                                    # Option C 관련 코드 (새로 추가됨)
│   ├── ConfigManager.java                     # 서버 설정 다운로드 및 캐시 관리
│   ├── CustomWebViewClient.java               # 서버 설정을 WebView에 적용
│   ├── WebViewHelper.java                     # WebView 초기화 헬퍼
│   └── INTEGRATION_EXAMPLE.java               # 통합 예제 코드
├── pattern/
│   └── common/
│       └── WebviewUpdatePatternMessage_OptionC.java  # WebView 업데이트 (서버 기반)
└── [기존 Zero 앱 코드...]
```

## 🚀 시작하기

### 1. 서버 설정

먼저 FastAPI 서버를 배포해야 합니다. 서버 코드는 [zero-ser-ver](https://github.com/mim1012/zero-ser-ver) 리포지토리에 있습니다.

```bash
# 서버 리포지토리 클론
git clone https://github.com/mim1012/zero-ser-ver.git

# Railway 또는 다른 플랫폼에 배포
# 배포 가이드는 zero-ser-ver/OPTION_C_DEPLOYMENT_GUIDE.md 참조
```

### 2. Android 프로젝트 설정

#### 2.1 서버 URL 설정

`INTEGRATION_EXAMPLE.java` 파일에서 서버 URL을 실제 배포된 주소로 변경하세요:

```java
private static final String SERVER_URL = "https://your-server.railway.app";
```

#### 2.2 기존 코드에 통합

`INTEGRATION_EXAMPLE.java`를 참고하여 기존 Activity에 다음 코드를 추가하세요:

```java
// 1. ConfigManager 초기화
ConfigManager configManager = ConfigManager.getInstance(this, SERVER_URL);

// 2. 서버에서 설정 다운로드
String deviceModel = Build.MODEL;
String chromeVersion = "143";
configManager.updateFromServer(deviceModel, chromeVersion);

// 3. WebView 초기화 (서버 설정 적용)
WebViewHelper.initializeWebView(this, webView, configManager);

// 4. URL 로드 (커스텀 헤더 포함)
CustomWebViewClient client = (CustomWebViewClient) webView.getWebViewClient();
client.loadUrlWithServerHeaders(webView, "https://m.shopping.naver.com/");
```

### 3. 빌드 및 실행

```bash
# Android Studio에서 프로젝트 열기
# Build > Make Project

# APK 빌드
./gradlew assembleRelease
```

## 🔧 설정 관리

### 서버에서 설정 변경

서버의 `app/config/` 폴더에 있는 JSON 파일을 수정하면 모든 기기에 자동으로 적용됩니다:

- `headers_default.json`: 기본 HTTP 헤더
- `user_agents.json`: User-Agent 설정
- `webview_settings.json`: WebView 설정

예시:
```json
{
  "user_agent": "Mozilla/5.0 (Linux; Android 13; SM-G998N) AppleWebKit/537.36 ...",
  "headers": {
    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "accept-language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
  },
  "webview_settings": {
    "javascript_enabled": true,
    "dom_storage_enabled": true,
    "cache_mode": "LOAD_DEFAULT"
  }
}
```

## 📚 상세 문서

- **서버 배포 가이드**: [zero-ser-ver/OPTION_C_DEPLOYMENT_GUIDE.md](https://github.com/mim1012/zero-ser-ver/blob/main/OPTION_C_DEPLOYMENT_GUIDE.md)
- **통합 가이드**: `INTEGRATION_GUIDE.md` (이 리포지토리)
- **API 문서**: 서버 실행 후 `https://your-server.railway.app/docs` 접속

## 🛠️ 기술 스택

- **Android**: Java, WebView, OkHttp
- **서버**: FastAPI, Python, Supabase
- **배포**: Railway (서버), APK (Android)

## 📝 라이선스

이 프로젝트는 원본 Zero 앱의 라이선스를 따릅니다.

## 🤝 기여

버그 리포트나 기능 제안은 이슈로 등록해주세요.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 생성해주세요.

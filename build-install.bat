@echo off
chcp 65001 > nul
cd /d "%~dp0"
set JAVA_HOME=D:\Android\jbr

echo ══════════════════════════════════════
echo   Zero Traffic — 빌드 + 설치 + 실행
echo ══════════════════════════════════════

:: ADB 연결 확인
adb devices | findstr /R "device$" > nul
if %errorlevel% neq 0 (
    echo [오류] ADB 연결된 기기 없음. USB 연결 + USB 디버깅 확인
    pause
    exit /b 1
)

:: 빌드
echo.
echo [1/3] APK 빌드 중...
call gradlew.bat assembleDebug --quiet
if %errorlevel% neq 0 (
    echo [오류] 빌드 실패
    pause
    exit /b 1
)
echo       빌드 완료

:: 설치
echo.
echo [2/3] 기기에 설치 중...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo [오류] 설치 실패
    pause
    exit /b 1
)
echo       설치 완료

:: 실행 (MY_PACKAGE_REPLACED가 자동 실행하지만 확실히 하기 위해 직접 실행)
echo.
echo [3/3] 앱 실행 중...
adb shell am start -n com.zero.traffic/.MainActivity --ez auto_start true
echo       실행 완료

echo.
echo ══════════════════════════════════════
echo   완료! 기기 화면을 확인하세요.
echo ══════════════════════════════════════
timeout /t 3 > nul

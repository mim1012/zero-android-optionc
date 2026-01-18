package com.sec.android.app.sbrowser.pattern.google.action;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.CharacterData;
import com.sec.android.app.sbrowser.pattern.InputData;

import java.util.ArrayList;
import java.util.List;

import static com.sec.android.app.sbrowser.pattern.Jaso.hangulToJaso;

public class GooglePatternAction extends BasePatternAction {

    private static final String TAG = GooglePatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "PatternAction";

    public GooglePatternAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);
    }

    public long getPreDelayMillis() {
        return MathHelper.randomRange(4000, 8000);
    }

    public void touchSearchBar(boolean isHome) {
        touchScreenParsed(250, isHome ? 300 : 150);
    }

    // 네이버 로고 버튼 터치.
    public void touchLogoButton() {
        touchScreenParsed(70, 150);
    }


    public void inputKeyword() {
        do {
            runNextKey();

            Log.d(TAG, "문자 " + (char)getCurrentInputData().unicode + " 입력 후 대기: " + getCurrentInputData().afterDelayMillis + "ms");
            SystemClock.sleep(getCurrentInputData().afterDelayMillis);
        } while (goNext());
    }

    public void touchSearchButton() {
        _touchInjector.touchKeyboard(-10);
    }

    public void touchHanButton() {
//        _touchInjector.touchScreen(210, 1800);
        _touchInjector.touchKeyboard(-1);
    }

    public void touchBackButton() {
//        KEYCODE_BACK
//        _touchInjector.touchKeyboard(0x0008);
        _touchInjector.keyBack();
    }






    // Keyboard 처리 부분...
    private int _currentIndex = 0;
    private int _currentInputIndex = 0;

    private List<CharacterData> _characterDataList = new ArrayList<>();
    boolean _touchMode = false;

    public void extractStrings(String string) {
        _characterDataList.clear();
        _currentIndex = 0;
        _currentInputIndex = 0;

        String _inputWords = hangulToJaso(string);

        convertChars(_inputWords);
    }

    public void convertChars(String s) { // 유니코드 한글 문자열을 입력 받음
        SamsungKeyboard keyboard = (SamsungKeyboard) _touchInjector.getSoftKeyboard();

        // 자소단위로 분리된 리스트다.
        // 먼저 인풋 정보들을 생성하기 위해 문자데이터를 등록한다.
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            CharacterData characterData = new CharacterData();
            characterData.setCharacter(keyboard, ch);
            _characterDataList.add(characterData);
        }

        // 인풋 정보들을 확인하여 추가 딜레이가 필요하다면 변경해준다.
        for (int i = 0; i < _characterDataList.size(); i++) {
            if (i == _characterDataList.size() - 1) {
                break;
            }

            CharacterData data = _characterDataList.get(i);
            CharacterData nextData = _characterDataList.get(i + 1);

//            Log.d(TAG, i + ": " + (char)data.getLastInputUnicode() + "/" + data.getLastInput().afterDelayMillis);

            // 다음 문자 스페이스면 딜레이타임을 더 길게 준다.
            if (keyboard.getButtonId(nextData.getFirstInput().unicode) == 16) {
                Log.d(TAG, "다음문자 스페이스: " + data.getLastInput().afterDelayMillis + "(" + (char)data.getLastInputUnicode() + ")");
                data.getLastInput().afterDelayMillis = MathHelper.randomRange(3000, 4000);
                continue;
            }

            // 버튼 아이디가 있고(0 이 아니고),
            // 다음 입력이 같다면(즉 같은 버튼을 눌러야 한다면) 2초 이상 대기한다.
            if ((keyboard.getButtonId(data.getLastInputUnicode()) != 0) &&
                    (keyboard.getButtonId(data.getLastInputUnicode()) == keyboard.getButtonId(nextData.getFirstInputUnicode()))) {
//                Log.d(TAG, "같은 키버튼 대기: " + data.getLastInput().afterDelayMillis + "(" + (char)data.getFirstInputUnicode() + ")");
                // 다음키가 같으므로 다음키의 대기시간을 늘려준다.
                data.getLastInput().afterDelayMillis = MathHelper.randomRange(3000, 4000);
                Log.d(TAG, "같은 키버튼 대기 후: " + data.getLastInput().afterDelayMillis + "(" + (char)data.getLastInputUnicode() + ")");
                continue;
            }
        }

        for (int i = 0; i < _characterDataList.size(); i++) {
            CharacterData data = _characterDataList.get(i);
            Log.d(TAG, i + ": " + (char)data.getLastInputUnicode() + "/" + data.getLastInput().afterDelayMillis);
        }
    }

    public InputData getCurrentInputData() {
        return _characterDataList.get(_currentIndex).getInputDataList().get(_currentInputIndex);
    }

    public boolean goNext() {
        ++_currentInputIndex;

        if (_currentInputIndex == _characterDataList.get(_currentIndex).getInputDataList().size()) {
            ++_currentIndex;
            _currentInputIndex = 0;
        }

        // 모두 돌았다.
        if (_currentIndex == _characterDataList.size()) {
            _characterDataList.clear();
            _currentIndex = 0;
            _currentInputIndex = 0;
            return false;
        }

        return true;
    }

    public long runNextKey() {
        SamsungKeyboard keyboard = (SamsungKeyboard) _touchInjector.getSoftKeyboard();

        int unicode = getCurrentInputData().unicode;

        boolean touchMode = keyboard.needTouch(unicode);

//        SystemClock.sleep(2000);
        if (_touchMode != touchMode) {
            // 2초 대기 추가
            Log.d(TAG, "다른 언어라서 2초 대기 추가");
            SystemClock.sleep(2);
        }

        if (touchMode) {
            _touchInjector.touchKeyboard(unicode);
        } else {
            _touchInjector.sendKeyboard(unicode);
        }

        _touchMode = touchMode;

        return 0;
    }

}

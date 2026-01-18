package com.sec.android.app.sbrowser.pattern;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;

import java.util.ArrayList;
import java.util.List;

public class CharacterData {
    private char _character;
    private List<InputData> _inputDataList =  new ArrayList<>();

    // 불필요 할듯 하다.
    public void setLangButton() {
        _character = 1;

        InputData inputData = new InputData();
        inputData.unicode = 21;
        inputData.afterDelayMillis = MathHelper.randomRange(150, 500);

        _inputDataList.add(inputData);
    }

    // 문자 설정하면서 인풋 정보 등록.
    public void setCharacter(SamsungKeyboard keyboard, char character) {
        _character = character;

        int[] converts = keyboard.convertReal(character);

        for (int j = 0; j < converts.length; j++) {
            InputData inputData = new InputData();
            inputData.unicode = converts[j];

            if (j < converts.length - 1) {
                // 버튼을 두번이상 터치해야하는 경우이므로 타임을 짧게 준다.
                inputData.afterDelayMillis = MathHelper.randomRange(500,600);
            } else {
                inputData.afterDelayMillis = MathHelper.randomRange(700, 900);
            }

            _inputDataList.add(inputData);
//            result = result + (char)converts[j];
        }
    }

    public InputData getFirstInput() {
        return _inputDataList.get(0);
    }

    public int getFirstInputUnicode() {
        return _inputDataList.get(0).unicode;
    }

    public InputData getLastInput() {
        return _inputDataList.get(_inputDataList.size() - 1);
    }

    public int getLastInputUnicode() {
        return _inputDataList.get(_inputDataList.size() - 1).unicode;
    }

//    public void updateDelayMillisForInputIndex(int index) {
//        _inputDataList.get(index).afterDelayMillis = MathHelper.randomRange(500, 1500);
//
//    }


    public List<InputData> getInputDataList() {
        return _inputDataList;
    }
}

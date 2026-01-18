package com.sec.android.app.sbrowser.keyboard;


import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import com.sec.android.app.sbrowser.engine.Utility;

public class SamsungKeyboard implements SoftKeyboard {

    @Override
    public Point getButtonPosition(int unicode) {
        Point point = null;

        switch (unicode) {
            case 0x3163:  // ㅣ
                point = new Point(150, 1250);
                break;
            case 0x3131:  // ㄱ
            case 0x314B:  // ㅋ
            case 0x3132:  // ㄲ
                point = new Point(150, 1450);
                break;
            case 0x3142:  // ㅂ
            case 0x314D:  // ㅍ
            case 0x3143:  // ㅃ
                point = new Point(150, 1625);
                break;
            case -2:  // 123,기호
                point = new Point(70, 1800);
                break;
            case -1:  // 한/영
                point = new Point(210, 1800);
                break;

            case 0x318D:  // .
                point = new Point(400, 1250);
                break;
            case 0x3134:  // ㄴ
            case 0x3139:  // ㄹ
                point = new Point(400, 1450);
                break;
            case 0x3145:  // ㅅ
            case 0x314E:  // ㅎ
            case 0x3146:  // ㅆ
                point = new Point(400, 1625);
                break;
            case 0x3147:  // ㅇ
            case 0x3141:  // ㅁ
                point = new Point(400, 1800);
                break;

            case 0x3161:  // ㅡ
                point = new Point(670, 1250);
                break;
            case 0x3137:  // ㄷ
            case 0x314C:  // ㅌ
            case 0x3138:  // ㄸ
                point = new Point(670, 1450);
                break;
            case 0x3148:  // ㅈ
            case 0x314A:  // ㅊ
            case 0x3149:  // ㅉ
                point = new Point(670, 1625);
                break;
            case 0x0020:  // SP
                point = new Point(670, 1800);
                break;

            case 0x0008:  // BS
                point = new Point(950, 1250);
                break;
            case -10:  // Search
                point = new Point(950, 1450);
                break;
            case 0x002E:  // .
            case 0x002C:  // ,
            case 0x003F:  // ?
            case 0x0021:  // !
                point = new Point(950, 1625);
                break;
        }

//        String x = "150", y = "1250";   // ㅣ
//        String x = "150", y = "1450";   // ㄱ,ㅋ,ㄲ
//        String x = "150", y = "1625";   // ㅂ,ㅍ,ㅃ
//        String x = "70", y = "1800";    // 123,기호
//        String x = "210", y = "1800";   // 한/영
//
//        String x = "400", y = "1250";   // .
//        String x = "400", y = "1450";   // ㄴ,ㄹ
//        String x = "400", y = "1625";   // ㅅ,ㅎ,ㅆ
//        String x = "400", y = "1800";   // ㅇ,ㅁ
//
//        String x = "670", y = "1250";   // ㅡ
//        String x = "670", y = "1450";   // ㄷ,ㅌ,ㄸ
//        String x = "670", y = "1625";   // ㅈ,ㅊ,ㅉ
//        String x = "400", y = "1800";   // SP
//
//        String x = "950", y = "1250";   // BS
//        String x = "950", y = "1450";   // Search
//        String x = "950", y = "1625";   // .,?!

        return point;
    }

    public int getButtonId(int unicode) {
        int buttonId = 0;

        switch (unicode) {
            case 0x3163:  // ㅣ
                buttonId = 1;
                break;
            case 0x3131:  // ㄱ
            case 0x314B:  // ㅋ
            case 0x3132:  // ㄲ
                buttonId = 5;
                break;
            case 0x3142:  // ㅂ
            case 0x314D:  // ㅍ
            case 0x3143:  // ㅃ
                buttonId = 9;
                break;
            case -2:  // 123,기호
                buttonId = 13;
                break;
            case 0x0015:  // 한/영
                buttonId = 14;
                break;

            case 0x318D:  // .
                buttonId = 2;
                break;
            case 0x3134:  // ㄴ
            case 0x3139:  // ㄹ
                buttonId = 6;
                break;
            case 0x3145:  // ㅅ
            case 0x314E:  // ㅎ
            case 0x3146:  // ㅆ
                buttonId = 10;
                break;
            case 0x3147:  // ㅇ
            case 0x3141:  // ㅁ
                buttonId = 15;
                break;

            case 0x3161:  // ㅡ
                buttonId = 3;
                break;
            case 0x3137:  // ㄷ
            case 0x314C:  // ㅌ
            case 0x3138:  // ㄸ
                buttonId = 7;
                break;
            case 0x3148:  // ㅈ
            case 0x314A:  // ㅊ
            case 0x3149:  // ㅉ
                buttonId = 11;
                break;
            case 0x0020:  // SP
                buttonId = 16;
                break;

            case 0x0008:  // BS
                buttonId = 4;
                break;
            case -10:  // Return, Search
                buttonId = 8;
                break;
            case 0x002E:  // .
            case 0x002C:  // ,
            case 0x003F:  // ?
            case 0x0021:  // !
                buttonId = 12;
                break;
        }

        return buttonId;
    }


    public static int tapCount(int unicode) {
        int count = 0;

        switch (unicode) {
            case 0x3163:  // ㅣ
            case 0x3131:  // ㄱ
            case 0x3142:  // ㅂ
            case -2:  // 123,기호
            case -1:  // 한/영
            case 0x318D:  // .
            case 0x3134:  // ㄴ
            case 0x3145:  // ㅅ
            case 0x3147:  // ㅇ
            case 0x3161:  // ㅡ
            case 0x3137:  // ㄷ
            case 0x3148:  // ㅈ
            case 0x0020:  // SP
            case 0x0008:  // BS
            case -10:  // Search
            case 0x002E:  // .
                count = 1;
                break;

            case 0x314B:  // ㅋ
            case 0x314D:  // ㅍ
            case 0x3139:  // ㄹ
            case 0x314E:  // ㅎ
            case 0x3141:  // ㅁ
            case 0x314C:  // ㅌ
            case 0x314A:  // ㅊ
            case 0x002C:  // ,
                count = 2;
                break;

            case 0x3132:  // ㄲ
            case 0x3143:  // ㅃ
            case 0x3146:  // ㅆ
            case 0x3138:  // ㄸ
            case 0x3149:  // ㅉ
            case 0x003F:  // ?
                count = 3;
                break;

            case 0x0021:  // !
                count = 4;
                break;

        }

        return count;
    }

    public boolean needTouch(int unicode) {
        boolean needTouch = false;

        switch (unicode) {
            case 0x3163:  // ㅣ
            case 0x3131:  // ㄱ
            case 0x3142:  // ㅂ
            case -2:  // 123,기호
            case -1:  // 한/영
            case 0x318D:  // .
            case 0x3134:  // ㄴ
            case 0x3145:  // ㅅ
            case 0x3147:  // ㅇ
            case 0x3161:  // ㅡ
            case 0x3137:  // ㄷ
            case 0x3148:  // ㅈ
            case 0x0020:  // SP
            case 0x0008:  // BS
            case -10:  // Search
            case 0x002E:  // .
            case 0x314B:  // ㅋ
            case 0x314D:  // ㅍ
            case 0x3139:  // ㄹ
            case 0x314E:  // ㅎ
            case 0x3141:  // ㅁ
            case 0x314C:  // ㅌ
            case 0x314A:  // ㅊ
            case 0x002C:  // ,
            case 0x3132:  // ㄲ
            case 0x3143:  // ㅃ
            case 0x3146:  // ㅆ
            case 0x3138:  // ㄸ
            case 0x3149:  // ㅉ
            case 0x003F:  // ?
            case 0x0021:  // !
                needTouch = true;
                break;
        }

        return needTouch;
    }

    public int[] convertReal(int unicode) {
        List<Integer> unicodeArray = new ArrayList<>();

        switch (unicode) {
//            case 0x3163:  // ㅣ
//            case 0x3131:  // ㄱ
//            case 0x3142:  // ㅂ
//            case -2:  // 123,기호
//            case -1:  // 한/영
//            case 0x318D:  // .
//            case 0x3134:  // ㄴ
//            case 0x3145:  // ㅅ
//            case 0x3147:  // ㅇ
//            case 0x3161:  // ㅡ
//            case 0x3137:  // ㄷ
//            case 0x3148:  // ㅈ
//            case 0x0020:  // SP
//            case 0x0008:  // BS
//            case -10:  // Search
//            case 0x002E:  // .
//                break;

            case 0x314B:  // ㅋ
            case 0x314D:  // ㅍ
            case 0x3139:  // ㄹ
            case 0x314E:  // ㅎ
            case 0x3141:  // ㅁ
            case 0x314C:  // ㅌ
            case 0x314A:  // ㅊ
            case 0x002C:  // ,
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                break;
            case 0x314f:    // ㅏ
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                break;
            case 0x3150:    // ㅐ
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x3151:    // ㅑ
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                break;
            case 0x3152:    // ㅒ
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x3153:    // ㅓ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x3154:    // ㅔ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                unicodeArray.add(0x3163);
                break;
            case 0x3155:    // ㅕ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x3156:    // ㅖ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                unicodeArray.add(0x3163);
                break;
            case 0x3157:    // ㅗ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3161);
                break;
            case 0x3158:    // ㅘ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3161);
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                break;
            case 0x3159:    // ㅙ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3161);
                unicodeArray.add(0x3163);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x315a:    // ㅚ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3161);
                unicodeArray.add(0x3163);
                break;
            case 0x315b:    // ㅛ
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3161);
                break;
            case 0x315c:    // ㅜ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x318D);
                break;
            case 0x315d:    // ㅝ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x315e:    // ㅞ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                unicodeArray.add(0x3163);
                break;
            case 0x315f:    // ㅟ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x3163);
                break;
            case 0x3160:    // ㅠ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x318D);
                unicodeArray.add(0x318D);
                break;
            case 0x3161:    // ㅡ
                unicodeArray.add(0x3161);
                break;
            case 0x3162:    // ㅢ
                unicodeArray.add(0x3161);
                unicodeArray.add(0x3163);
                break;
            case 0x3163:    // ㅣ
                unicodeArray.add(0x3163);
                break;

            case 0x3132:  // ㄲ
            case 0x3143:  // ㅃ
            case 0x3146:  // ㅆ
            case 0x3138:  // ㄸ
            case 0x3149:  // ㅉ
            case 0x003F:  // ?
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                break;

                // 받침 두개 짜리는 종성을 분리하는것이 좋긴하다.
            case 0x3133:  // ㄳ
                unicodeArray.add(0x3131);
                unicodeArray.add(0x3145);
                break;

            case 0x3135:  // ㄵ
                unicodeArray.add(0x3134);
                unicodeArray.add(0x3148);
                break;

            case 0x3136:  // ㄶ
                unicodeArray.add(0x3134);
                unicodeArray.add(0x314e);
                unicodeArray.add(0x314e);
                break;

            case 0x313A:  // ㄺ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3131);
                break;

            case 0x313b:  // ㄻ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3141);
                unicodeArray.add(0x3141);
                break;

            case 0x313c:  // ㄼ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3142);
                break;

            case 0x313d:  // ㄽ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3145);
                break;

            case 0x313e:  // ㄾ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x314c);
                unicodeArray.add(0x314c);
                break;

            case 0x313f:  // ㄿ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x314d);
                unicodeArray.add(0x314d);
                break;

            case 0x3140:  // ㅀ
                unicodeArray.add(0x3139);
                unicodeArray.add(0x3139);
                unicodeArray.add(0x314e);
                unicodeArray.add(0x314e);
                break;

            case 0x3144:  // ㅄ
                unicodeArray.add(0x3142);
                unicodeArray.add(0x3145);
                break;

            case 0x0021:  // !
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                unicodeArray.add(unicode);
                break;

            default:
                unicodeArray.add(unicode);
                break;
        }

        return Utility.convertIntegers(unicodeArray);
    }

//                                      // ㄱ      ㄲ       ㄴ      ㄷ       ㄸ      ㄹ       ㅁ      ㅂ       ㅃ       ㅅ      ㅆ       ㅇ      ㅈ       ㅉ      ㅊ       ㅋ      ㅌ       ㅍ      ㅎ
//    final static char[] ChoSung   = { 0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };
//                                      // ㅏ      ㅐ       ㅑ      ㅒ       ㅓ      ㅔ       ㅕ      ㅖ       ㅗ       ㅘ      ㅙ       ㅚ      ㅛ       ㅜ      ㅝ       ㅞ      ㅟ       ㅠ      ㅡ       ㅢ      ㅣ
//    final static char[] JwungSung = { 0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163 };
//                                      //        ㄱ       ㄲ      ㄳ       ㄴ      ㄵ       ㄶ      ㄷ      ㄹ      ㄺ      ㄻ      ㄼ      ㄽ      ㄾ      ㄿ      ㅀ      ㅁ      ㅂ      ㅄ      ㅅ      ㅆ      ㅇ      ㅈ      ㅊ      ㅋ      ㅌ      ㅍ      ㅎ
//    final static char[] JongSung  = { 0,      0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };


}

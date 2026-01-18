package com.sec.android.app.sbrowser.keyboard;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.SuCommander;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TouchInjector {

    private static final String TAG = TouchInjector.class.getSimpleName();

    private Context _context;
    private SoftKeyboard _keyboard = null;
    private Thread _thread = null;

    public TouchInjector(Context context) {
        _context = context;
    }

    public SoftKeyboard getSoftKeyboard() {
        return _keyboard;
    }

    public void setSoftKeyboard(SoftKeyboard keyboard) {
        _keyboard = keyboard;
    }

    public int getParsedX(int x) {
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
        int baseWidth = 1080;
        float widthRatio = metrics.widthPixels * 1.0f / baseWidth;
        return Math.round(x * widthRatio);
    }

    public int getParsedY(int y) {
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
        int baseHeight = 1920;
        float heightRatio = metrics.heightPixels * 1.0f / baseHeight;
        return Math.round(y * heightRatio);
    }

    public void touchScreen(int x, int y) {
//        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
//        float dpRatio = metrics.density;
//        int baseWidth = 1080;
//        int baseHeight = 1920;
//
//        float widthRatio = metrics.widthPixels * 1.0f / baseWidth;
//        float heightRatio = metrics.heightPixels * 1.0f / baseHeight;
//
//        final String xy = String.format("%s %s", Math.round(x * widthRatio), Math.round(y * heightRatio));
        final String xy = String.format("%s %s", getParsedX(x), getParsedY(y));

//        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                String x = "400", y = "1450";

                try {
                    String cmd = "input tap " + xy;
//                    String cmd = "/system/bin/input tap " + xy + "\n";
                    SuCommander.execute(cmd);
                    Log.d(TAG, "터치: " + xy);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        _thread.start();

//        threadStartJoin();
    }

    public void touchScreen2(final int x, final int y) {
        final String xy = String.format("%s %s", x, y);

        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                String x = "400", y = "1450";

                try {
                    // ABS_MT_TOUCH_MAJOR 48 : 0X30
                    // ABS_MT_TOUCH_MINOR 49 : 0X31
                    // ABS_MT_WIDTH_MAJOR 50 : 0X32
                    // ABS_MT_WIDTH_MINOR 51 : 0X33
                    String event = "sendevent /dev/input/event3 ";
                    String eventAbs = event + "3 ";
                    String eventAbsId = eventAbs + "57 ";
                    String eventAbsX = eventAbs + "53 ";
                    String eventAbsY = eventAbs + "54 ";
                    String eventAbsTouchMajor = eventAbs + "48 ";
                    String eventAbsTouchMinor = eventAbs + "49 ";
                    String eventAbsWidthMajor = eventAbs + "50 ";
                    String eventAbsWidthMinor = eventAbs + "51 ";
                    String eventKey = event + "1 ";
                    String eventKeyBtnTouch = eventKey + "330 ";
                    String eventKeyBtnTouchDown = eventKeyBtnTouch + "1;";
                    String eventKeyBtnTouchUp = eventKeyBtnTouch + "0;";
                    String eventSyn = event + "0 0 0;";

//                    String cmd = eventAbsId + "" +  MathHelper.randomRange(10, 1000) + ";" +       // EV_ABS       ABS_MT_TRACKING_ID   00000001
                    String cmd = eventAbsId + "1;" +   // EV_ABS       ABS_MT_TRACKING_ID   00000001
                            eventKeyBtnTouchDown +      // EV_KEY       BTN_TOUCH            DOWN
                            eventAbsX + x + ";" +      // EV_ABS       ABS_MT_POSITION_X
                            eventAbsY + y + ";";       // EV_ABS       ABS_MT_POSITION_Y

                    String cmdAll = cmd;

                    // 명령어가 무시되는것을 방지 하기 위해 다운신호를 먼저 보낸다.
//                    SuCommander.execute(cmd);

                    cmd = getRandomPatternString();
                    cmdAll += cmd;
//                    SuCommander.execute(cmd);

                    cmd = eventAbsId + "-1;" +    // EV_ABS       ABS_MT_TRACKING_ID   ffffffff
                            eventKeyBtnTouchUp +    // EV_KEY       BTN_TOUCH            UP
                            eventSyn;               // EV_SYN       SYN_REPORT           00000000

                    cmdAll += cmd;
//                    SuCommander.execute(cmd);
                    SuCommander.execute(cmdAll);

                    Log.d(TAG, "cmd: " + cmdAll);
                    Log.d(TAG, "터치21: " + xy);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

//        _thread.start();

        threadStartJoin();
    }

    private String getRandomPatternString() {
        List<String> patternList = new ArrayList<>();
//        patternList.add()

        // ABS_MT_TOUCH_MAJOR 48 : 0X30
        // ABS_MT_TOUCH_MINOR 49 : 0X31
        // ABS_MT_WIDTH_MAJOR 50 : 0X32
        // ABS_MT_WIDTH_MINOR 51 : 0X33
        String event = "sendevent /dev/input/event3 ";
        String eventAbs = event + "3 ";
        String eventAbsId = eventAbs + "57 ";
        String eventAbsX = eventAbs + "53 ";
        String eventAbsY = eventAbs + "54 ";
        String eventAbsTouchMajor = eventAbs + "48 ";
        String eventAbsTouchMinor = eventAbs + "49 ";
        String eventAbsWidthMajor = eventAbs + "50 ";
        String eventAbsWidthMinor = eventAbs + "51 ";
        String eventKey = event + "1 ";
        String eventKeyBtnTouch = eventKey + "330 ";
        String eventKeyBtnTouchDown = eventKeyBtnTouch + "1;";
        String eventKeyBtnTouchUp = eventKeyBtnTouch + "0;";
        String eventSyn = event + "0 0 0;";

        patternList.add(eventAbsTouchMinor + "5;" + eventAbsWidthMajor + "4;" + eventSyn +
                eventAbsWidthMajor + "7;" + eventSyn +
                eventAbsWidthMajor + "8;" + eventSyn +
                eventAbsWidthMajor + "6;" + eventSyn);

        patternList.add(eventAbsWidthMajor + "8;" + eventSyn +
                eventAbsWidthMajor + "10;" + eventSyn +
                eventAbsWidthMajor + "9;" + eventSyn);

        patternList.add(eventAbsTouchMajor + "6;" + eventAbsWidthMajor + "4;" + eventSyn +
                eventAbsTouchMajor + "7;" + eventAbsWidthMajor + "9;" + eventSyn +
                eventAbsWidthMajor + "10;" + eventSyn);

        // 요거 잘안딘다.
//        patternList.add(eventAbsWidthMajor + "2;" + eventSyn +
//                eventAbsWidthMajor + "12;" + eventSyn +
//                eventAbsTouchMinor + "6;" + eventAbsWidthMajor + "11;" + eventSyn +
//                eventAbsTouchMinor + "7;" + eventAbsWidthMajor + "10;" + eventSyn);
//
//        patternList.add(eventAbsWidthMajor + "5;" + eventSyn +
//                eventAbsWidthMajor + "10;" + eventSyn +
//                eventAbsWidthMajor + "11;" + eventSyn +
//                eventAbsWidthMajor + "10;" + eventSyn +
//                eventAbsTouchMinor + "6;" + eventSyn +
//                eventAbsTouchMajor + "6;" + eventSyn);


        int patternIndex = (int) MathHelper.randomRange(0, patternList.size() - 1);
        patternIndex = 1;
        Log.d(TAG, "pattern index: " + patternIndex);
        return patternList.get(patternIndex);
    }


    public void touchScreenLong(int x, int y) {
        // 랜덤의 수치를 부여한다. 움직이면 안된다. 그럼 swipe 로 처리된다.
//        int x2 = x + (int) MathHelper.randomRange(0, 20) - 10;
//        int y2 = y + (int) MathHelper.randomRange(0, 20) - 10;
//
//        swipeScreen(x, y, x2, y2, MathHelper.randomRange(80, 500));
        swipeScreen(x, y, x, y, MathHelper.randomRange(80, 120));
    }

    public void touchScreenLong(int x, int y, long duration) {
        swipeScreen(x, y, x, y, duration);
    }

    public int randomRangePoint(int point, int range) {
        int result = point + (int) (MathHelper.randomRange(0, range) - (range * 0.5));

        if (result < 0) {
            result = 0;
        }

        return result;
    }

    // duration 값에 따라 스크롤 폭이 달라진다.
    // 100 이면 100ms
    // 1초 이상이면 더 큰 범위에서 움직인다.
    public void swipeScreen(boolean down, long duration) {
        int pointX = (int) MathHelper.randomRange(300, 1000);
        int startPointY = (int) MathHelper.randomRange(400, 600);
        int endPointY = startPointY;

        if (duration > 1000) {
//            endPointY = (int) MathHelper.randomRange(1100, 1600);
            endPointY += (int) MathHelper.randomRange(800, 950);
        } else {
//            endPointY = (int) MathHelper.randomRange(900, 1100);
            endPointY += (int) MathHelper.randomRange(400, 500);
        }

        if (down) {
            swipeScreen2(pointX, endPointY, randomRangePoint(pointX, 60), startPointY, duration);
        } else {
            swipeScreen2(pointX, startPointY, randomRangePoint(pointX, 60), endPointY, duration);
        }
    }

    public void swipeScreenFast(boolean down, long duration) {
        int pointX = (int) MathHelper.randomRange(300, 1000);
        int startPointY = (int) MathHelper.randomRange(400, 600);
        int endPointY = startPointY;

        if (duration > 1000) {
//            endPointY = (int) MathHelper.randomRange(1100, 1600);
            endPointY += (int) MathHelper.randomRange(800, 950);
        } else {
//            endPointY = (int) MathHelper.randomRange(900, 1100);
            endPointY += (int) MathHelper.randomRange(800, 900);
        }

        if (down) {
            swipeScreen2(pointX, endPointY, randomRangePoint(pointX, 60), startPointY, duration);
        } else {
            swipeScreen2(pointX, startPointY, randomRangePoint(pointX, 60), endPointY, duration);
        }
    }

    public void swipeScreenV(int y1, int y2, long duration) {
        int pointX = (int) MathHelper.randomRange(300, 900);
        swipeScreen(pointX, y1, randomRangePoint(pointX, 60), y2, duration);
    }

    public void swipeScreenH(int x1, int x2, long duration) {
        int pointY = (int) MathHelper.randomRange(300, 1600);
        swipeScreen(x1, pointY, x2, randomRangePoint(pointY, 60), duration);
    }

    public void swipeScreen(int x1, int y1, int x2, int y2, long duration) {
//        final String xy = String.format(Locale.getDefault(), "%d %d %d %d %d", x1, y1, x2, y2, duration);
        final String xy = String.format(Locale.getDefault(), "%d %d %d %d %d", getParsedX(x1), getParsedY(y1), getParsedX(x2), getParsedY(y2), duration);

        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "input swipe " + xy;
//                    String cmd = "/system/bin/input tap " + xy + "\n";
                    SuCommander.execute(cmd);
                    Log.d(TAG, "스와이프: " + xy);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadStartJoin();
    }

    public void swipeScreen2(int x1, int y1, int x2, int y2, long duration) {
//        final String xy = String.format(Locale.getDefault(), "%d %d %d %d %d", x1, y1, x2, y2, duration);
        final String xy = String.format(Locale.getDefault(), "%d %d %d %d %d", getParsedX(x1), getParsedY(y1), getParsedX(x2), getParsedY(y2), duration);

        Log.d(TAG, "스와이프: " + xy);
        MonkeyScript monkeyScript = new MonkeyScript(_context);
        monkeyScript.runSwipeParsed(x1, y1, x2, y2, duration);

//        threadJoin();
//
//        _thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String cmd = "input swipe " + xy;
////                    String cmd = "/system/bin/input tap " + xy + "\n";
//                    SuCommander.execute(cmd);
//                    Log.d(TAG, "스와이프: " + xy);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        threadStartJoin();
    }

    public void keyBack() {
        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "input keyevent KEYCODE_BACK";
                    SuCommander.execute(cmd);
                    Log.d(TAG, "뒤로");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadStartJoin();
    }


    public void touchKeyboard(int unicode) {
        if (_keyboard == null) {
            return;
        }

        Point point = _keyboard.getButtonPosition(unicode);
        touchScreen(point.x, point.y);
    }

    public void touchKeyboardLong(int unicode, long duration) {
        if (_keyboard == null) {
            return;
        }

        Point point = _keyboard.getButtonPosition(unicode);
        swipeScreen(point.x, point.y, point.x, point.y, duration);
    }

    public void sendKeyboardString(String string) {
        MonkeyScript monkeyScript = new MonkeyScript(_context);
        monkeyScript.runInputStringScript(string);
    }

    public void sendKeyboard(int unicode) {
        String charString = Character.toString((char)unicode);
        if (charString.equals(" ")) {
            charString = "%s";
        } else if (charString.equals("\"") ||
                charString.equals("'") ||
                charString.equals("|") ||
                charString.equals("&") ||
                charString.equals("#") ||
                charString.equals("\\")) {
            charString = "\\" + charString;
        }

        final String text = charString;

        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "/system/bin/input text " + text;
                    SuCommander.execute(cmd);
                    Log.d(TAG, "input text: " + text);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                String text = String.valueOf(0x0049);
//                new Instrumentation().sendStringSync(text);

//                new Instrumentation().sendStringSync("sjan!$jsrglㄲ안");
//                mWebView.
//                KeyEvent ev = new KeyEvent(SystemClock.uptimeMillis(), "ㄱ", KeyCharacterMap.VIRTUAL_KEYBOARD,0);
//                KeyEvent ev = new KeyEvent(KeyEvent.ACTION_DOWN, 0x132);
//                new Instrumentation().sendKeySync(ev);
//
////                KeyEvent ev1 = new KeyEvent(SystemClock.uptimeMillis(), "ㄱ", KeyCharacterMap.VIRTUAL_KEYBOARD,0);
//                KeyEvent ev1 = new KeyEvent(KeyEvent.ACTION_UP, 0x132);
//                new Instrumentation().sendKeySync(ev1);
//                new Instrumentation().sendStringSync();
//                new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_A);
//
//                KeyEvent ev = new KeyEvent(SystemClock.uptimeMillis(), "ㄱ", 0, 0);
//                dispatchKeyEvent(ev);

            }
        });

        threadStartJoin();
    }

    public void sendString(String string) {
        final String text = string.trim();

        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "/system/bin/input text \"" + text + "\"";
                    SuCommander.execute(cmd);
                    Log.d(TAG, "input text: " + text);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadStartJoin();
    }

    public void sendPasteClipboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            threadJoin();

            _thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String cmd = "/system/bin/input keyevent 279";
                        SuCommander.execute(cmd);
                        Log.d(TAG, "paste clipboard");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            threadStartJoin();
        }
    }

    private void threadStartJoin() {
        if (_thread != null) {
            _thread.start();
            threadJoin();
        }
    }

    private void threadJoin() {
        if (_thread != null) {
            try {
                _thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _thread = null;
        }
    }


    // Sample
    void testTouchKeyboard()
    {
//        int x = 400;
//        int y = 1450;
//
//        Instrumentation ints = InstrumentationR
//        Instrumentation inst = new Instrumentation();
//        UiAutomation automation = inst.getUiAutomation();
//
//        injectClickEvent(x, y, inst.getUiAutomation());

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int x = 400;
//                int y = 1450;
//
//                long time = SystemClock.uptimeMillis();
//                MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN,
//                        x, y, 0);
//                MotionEvent event1 = MotionEvent.obtain(time + 100, time + 100, MotionEvent.ACTION_UP,
//                        x, y, 0);
//
//                Instrumentation instrumentation = new Instrumentation();
//                instrumentation.getUiAutomation().injectInputEvent(event, true);
//                instrumentation.getUiAutomation().injectInputEvent(event1, true);
//            }
//        }).start();


//        int x = 100;
//        int y = 100;
//
//        try {
//            executeCommand(String.format(Locale.getDefault(), "input tap %d %d", x, y));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                String x = "400", y = "1450";

                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    String cmd = "/system/bin/input tap 400 1450\n";
                    os.writeBytes(cmd);
                    os.writeBytes("exit\n");
                    os.flush();
                    os.close();
                    process.waitFor();

                    Log.d(TAG, "터치: " + x + ", " + y);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        String x = "100", y = "100";
//
//        String[] deviceCommands = {"su", "input touchscreen tap", x, y};
//
//        try {
//            Process process = Runtime.getRuntime().exec(deviceCommands);
//            try {
//                process.waitFor();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "error!", Toast.LENGTH_SHORT).show();
//        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Instrumentation inst = new Instrumentation();
//
//                long time = SystemClock.uptimeMillis();
//
//                int x = 400;
//                int y = 1450;
//
//                MotionEvent event = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN,
//                        x, y, 0);
//
//                MotionEvent event1 = MotionEvent.obtain(time + 100, time + 100, MotionEvent.ACTION_UP,
//                        x, y, 0);
//
//                inst.sendPointerSync(event);
//                inst.sendPointerSync(event1);
//
//                Log.d(TAG, "터치: " + x + ", " + y);
//            }
//        }).start();


//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//            }
//        }.;


//        try {
//            eventInput();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, SystemClock.uptimeMillis(),
//                    400, 1450, 0);
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

}

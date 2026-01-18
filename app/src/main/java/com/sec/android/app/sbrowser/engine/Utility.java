package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.sec.android.app.sbrowser.R;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by K2Y on 2017. 7. 6..
 */

public class Utility {

    public static void showAlert(@NonNull Context context, @StringRes int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(messageId)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

    public static void showAlert(@NonNull Context context, @StringRes int titleId, @StringRes int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

    public static void showAlert(@NonNull Context context, @StringRes int titleId, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(titleId)
                .setMessage(message)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

    public static void showAlert(@NonNull Context context, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

    public static void showAlertNoCancel(@NonNull Context context, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.setCancelable(false);
        builder.show();
    }

    public static void showAlert(@NonNull Context context, CharSequence title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

    public static void showAlert(@NonNull Context context, CharSequence title, @StringRes int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title)
                .setMessage(messageId)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.show();
    }

//    public static void showGuideAlert(@NonNull Context context, @StringRes int textId) {
//        showAlert(context, R.string.title_guide, textId);
//    }
//
//    public static void showGuideAlert(@NonNull Context context, CharSequence message) {
//        showAlert(context, R.string.title_guide, message);
//    }

//    public static void showConfirmAlert(@NonNull Context context, @StringRes int messageId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        builder.setMessage(messageId)
//                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                })
//                .setNegativeButton(R.string.btn_register_cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });
//
//        builder.show();
//    }
//
//    public static void showAlert(@NonNull Context context, @StringRes int titleId, @StringRes int messageId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        builder.setTitle(titleId)
//                .setMessage(messageId)
//                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });
//
//        builder.show();
//    }

//    public static void showTalkChargeAlert(@NonNull final Context context) {
//        showChargeAlert(context, context.getText(R.string.msg_talk_point_charge));
//    }
//
//    public static void showGiftChargeAlert(@NonNull final Context context) {
//        showChargeAlert(context, context.getText(R.string.msg_gift_point_charge));
//    }

//    public static void showChargeAlert(@NonNull final Context context,
//                                       @Nullable CharSequence message) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        builder.setTitle(R.string.title_guide)
//                .setMessage(message)
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        Intent intent = new Intent(context, PointChargeActivity.class);
//                        context.startActivity(intent);
//                    }
//                })
//                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // UserData cancelled the dialog
//                    }
//                });
//
//        builder.show();
//    }



    // 0: man, 1: woman
//    public static int getSexColor(int sex) {
//        if (sex == 0) {
//            return R.color.colorMan;
//        } else {
//            return R.color.colorWoman;
//        }
//    }
//
//    public static String getLocalizedNicknameString(Context context, BaseContentItem item) {
//        return getLocalizedNicknameString(context, item.nickname, item.sex, item.age);
//    }
//
//    public static String getLocalizedNicknameString(Context context, String nickname, int sex, int age) {
//        String sexAbbr;
//        if (sex == 0) {
//            sexAbbr = context.getString(R.string.man_abbr);
//        } else {
//            sexAbbr = context.getString(R.string.woman_abbr);
//        }
//
//        return String.format(context.getString(R.string.format_nickname),
//                nickname,
//                sexAbbr,
//                age);
//    }
//
//    public static String getLocalizedNicknameString(Context context, String nickname, int sex, int age, String distance) {
//        String sexAbbr;
//        if (sex == 0) {
//            sexAbbr = context.getString(R.string.man_abbr);
//        } else {
//            sexAbbr = context.getString(R.string.woman_abbr);
//        }
//
//        return String.format(context.getString(R.string.format_nickname_ext),
//                nickname,
//                sexAbbr,
//                age,
//                distance);
//    }
//
//    public static String getLocalizedElapsedTimeStringFromDateString(Context context, String dateString) {
//        long now = System.currentTimeMillis();
//        long itemTime = 0;
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String time;
//
//        try {
//            Date itemDate = dateFormat.parse(dateString);
//            itemTime = itemDate.getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        long elapsedTime = (now - itemTime) / 1000; // ms to s.
//
//        if (elapsedTime < 3) {              // 방금전
//            time = context.getString(R.string.format_just);
//        } else if (elapsedTime < 60) {      // 몇초 전
//            time = String.format(context.getString(R.string.format_secs), elapsedTime);
//        } else if (elapsedTime < 3600) {    // 몇분 전: 60 * 60
//            time = String.format(context.getString(R.string.format_minutes), elapsedTime / 60);
//        } else if (elapsedTime < 7200) {    // 약 1시간 전: 60 * 60 * 2
//            time = context.getString(R.string.format_hour);
//        } else if (elapsedTime < 86400) {   // 몇시간 전: 60 * 60 * 24
//            time = String.format(context.getString(R.string.format_hours), elapsedTime / 3600);
//        } else if (elapsedTime < 172800) {  // 어제: 60 * 60 * 24 * 2
//            time = context.getString(R.string.yesterday);
//        } else {                            // 몇일 전
//            time = String.format(context.getString(R.string.format_days), elapsedTime / 86400);
//        }
//
//        return time;
//    }
//
//    @Nullable
//    public static Date getDateFromDateString(String dateString) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        try {
//            Date itemDate = dateFormat.parse(dateString);
//            return itemDate;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public static String getFullDateStringFromDate(Date date) {
////        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MMM ddd EEEE", Locale.getDefault());
//        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
//        return dateFormat.format(date);
//    }
//
//    public static String getDateStringFromDate(Date date) {
////        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MMM ddd EEEE", Locale.getDefault());
//        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
//        return dateFormat.format(date);
//    }
//
//    public static String getTimeStringFromDateString(String dateString) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        try {
//            Date date = dateFormat.parse(dateString);
//
//            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
////            SimpleDateFormat df = new SimpleDateFormat("aa HH:mm", Locale.getDefault());
//            return df.format(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//
//        return "";
//    }
//
//
//    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
//        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
//                .getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(output);
//
//        final int color = 0xff424242;
//        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        final RectF rectF = new RectF(rect);
//        final float roundPx = pixels;
//
//        paint.setAntiAlias(true);
//        canvas.drawARGB(0, 0, 0, 0);
//        paint.setColor(color);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
//
//        return output;
//    }

    public static String getRandomString(int length) {
        return getRandomString(length, null);
    }

    public static String getRandomString(int length, String postfix) {
        String generatedString = RandomStringUtils.randomAlphanumeric(length);
        return generatedString + (TextUtils.isEmpty(postfix) ? "" : postfix);
    }

    public static String getRandomString(int minLengthInclusive, int maxLengthExclusive) {
        return getRandomString(minLengthInclusive, maxLengthExclusive, null);
    }

    public static String getRandomString(int minLengthInclusive, int maxLengthExclusive, String postfix) {
        String generatedString = RandomStringUtils.randomAlphanumeric(minLengthInclusive, maxLengthExclusive);
        return generatedString + (TextUtils.isEmpty(postfix) ? "" : postfix);
    }

    public static String getRandomStringStartAlpha(int minLengthInclusive, int maxLengthExclusive) {
        return getRandomStringStartAlpha(minLengthInclusive, maxLengthExclusive, null);
    }

    public static String getRandomStringStartAlpha(int minLengthInclusive, int maxLengthExclusive, String postfix) {
        String prefix = RandomStringUtils.randomAlphabetic(1);
        String generatedString = RandomStringUtils.randomAlphanumeric(minLengthInclusive - 1, maxLengthExclusive - 1);
        return prefix + generatedString + (TextUtils.isEmpty(postfix) ? "" : postfix);
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            ret[i++] = value.intValue();
        }

        return ret;
    }


    private static final String ALLOWED_CHARACTERS ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890/+";

    public static String getRandomStringNew(final int sizeOfRandomString)
    {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return sb.toString();
    }
}


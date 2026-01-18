package com.sec.android.app.sbrowser.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

public class BitmapUtil {

    public static Bitmap getScaledBitmap(String pathName, int maxWidth, int maxHeight) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/maxWidth, photoH/maxHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(pathName, bmOptions);

        // 이미지를 상황에 맞게 회전시킨다
        try {
            ExifInterface exif = new ExifInterface(pathName);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            return rotate(scaledBitmap, exifDegree);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    public static int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else {
            return 0;
        }
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap 비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float)bitmap.getWidth() / 2,
                    (float)bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }

        return bitmap;
    }

    public static Bitmap resizeBitmapAspectFill(Bitmap src, int maxWidth, int maxHeight) {
        if (src == null) {
            return null;
        }

        int width = src.getWidth();
        int height = src.getHeight();

        // Determine how much to scale down the image
        float scaleFactor = Math.max(maxWidth / (float) width, maxHeight / (float) height);

        width = (int) (width * scaleFactor);
        height = (int) (height * scaleFactor);

        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    /**
     * Bitmap을 ratio에 맞춰서 max값 만큼 resize한다.
     *
     * @param Bitmap 원본
     * @param max 원하는 크기의 값
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap src, int max) {
        if(src == null)
            return null;

        int width = src.getWidth();
        int height = src.getHeight();
        float rate = 0.0f;

        if (width > height) {
            rate = max / (float) width;
            height = (int) (height * rate);
            width = max;
        } else {
            rate = max / (float) height;
            width = (int) (width * rate);
            height = max;
        }

        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    /**
     * Bitmap을 ratio에 맞춰서 max값 만큼 resize한다.
     *
     * @param src
     * @param max
     * @param isKeep 작은 크기인 경우 유지할건지 체크..
     * @return
     */
    public static Bitmap resize(Bitmap src, int max, boolean isKeep) {
        if(!isKeep)
            return resizeBitmap(src, max);

        int width = src.getWidth();
        int height = src.getHeight();
        float rate = 0.0f;

        if (width > height) {
            if (max > width) {
                rate = max / (float) width;
                height = (int) (height * rate);
                width = max;
            }
        } else {
            if (max > height) {
                rate = max / (float) height;
                width = (int) (width * rate);
                height = max;
            }
        }

        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    /**
     * Bitmap 이미지를 정사각형으로 만든다.
     *
     * @param src 원본
     * @param max 사이즈
     * @return
     */
    public static Bitmap resizeSquare(Bitmap src, int max) {
        if(src == null)
            return null;

        return Bitmap.createScaledBitmap(src, max, max, true);
    }


    /**
     * Bitmap 이미지를 가운데를 기준으로 w, h 크기 만큼 crop한다.
     *
     * @param src 원본
     * @param w 넓이
     * @param h 높이
     * @return
     */
    public static Bitmap cropCenterBitmap(Bitmap src, int w, int h) {
        if(src == null)
            return null;

        int width = src.getWidth();
        int height = src.getHeight();

        if(width < w && height < h)
            return src;

        int x = 0;
        int y = 0;

        if (width > w) {
            x = (width - w)/2;
        }

        if (height > h) {
            y = (height - h)/2;
        }

        int cw = w; // crop width
        int ch = h; // crop height

        if (w > width) {
            cw = width;
        }

        if (h > height) {
            ch = height;
        }

        return Bitmap.createBitmap(src, x, y, cw, ch);
    }

    public static Bitmap cropTopBitmap(Bitmap src, int w, int h) {
        if (src == null) {
            return null;
        }

        int width = src.getWidth();
        int height = src.getHeight();

        if (width < w && height < h) {
            return src;
        }

        int x = 0;
        int y = 0;

        if (width > w) {
            x = (width - w)/2;
        }

        int cw = w; // crop width
        int ch = h; // crop height

        if (w > width) {
            cw = width;
        }

        if (h > height) {
            ch = height;
        }

        return Bitmap.createBitmap(src, x, y, cw, ch);
    }
}

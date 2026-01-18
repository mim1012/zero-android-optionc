package com.sec.android.app.sbrowser.engine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SimpleScreenCapture {

    private static final String TAG = SimpleScreenCapture.class.getSimpleName();

    private static class LazyHolder {
        public static final SimpleScreenCapture INSTANCE = new SimpleScreenCapture();
    }

    public static SimpleScreenCapture getInstance() {
        return SimpleScreenCapture.LazyHolder.INSTANCE;
    }

    private Activity _activity = null;

    public void init(Activity activity) {
        _activity = activity;
    }

    public void setActivity(Activity activity) {
        _activity = activity;
    }

    public boolean takeScreenShot(String path) {
        if (_activity == null) {
            return false;
        }

        boolean success = false;
        View view = _activity.getWindow().getDecorView();
//        View view = _activity.getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bitmap.recycle();
        bitmap = null;

        return success;

//        fos = new FileOutputStream(_path);
//        cropped.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//        cropped.recycle();


//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap b1 = view.getDrawingCache();
//        Rect frame = new Rect();
//        view.getWindowVisibleDisplayFrame(frame);
//        int statusBarHeight = frame.top;
//
//        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
//
//        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
//        view.destroyDrawingCache();
//        return b;
    }
}

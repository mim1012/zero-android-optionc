package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sec.android.app.sbrowser.library.image.ImageMatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageFinder {

    private static final String TAG = ImageFinder.class.getSimpleName();

    private static class LazyHolder {
        public static final ImageFinder INSTANCE = new ImageFinder();
    }

    public static ImageFinder getInstance() {
        return LazyHolder.INSTANCE;
    }

    private final ThreadMutex _mutex = new ThreadMutex();
    private Context _context = null;
    private ImageMatcher _im = null;
    private ScreenCapture _capture = null;

    public void init(Context context, boolean useScreenCapture) {
        _context = context;
        initMatcher();

        if (useScreenCapture) {
            initCapture(context);
        }
    }

    public void close() {
        if (_im != null) {
            _im.close();
        }

        if (_capture != null) {
            _capture.stopCapture();
        }
    }

    public ScreenCapture getScreenCapture() {
        return _capture;
    }

    private String getFilepath() {
        return Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getPath() + "/capture.png";
    }

    public String takeScreenshot() {
        Log.d(TAG, "화면캡쳐!!");

        if (_capture != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    _capture.captureAsync(getFilepath());
                }
            });
            _mutex.threadWaitInfinite();

            return getFilepath();
        } else {
            return ScreenCaptureInjector.takeScreenshot(getFilepath());
        }
    }

    public void findUrlImage(FinderCallback callback, int width, int height, String url) {
        findUrlImageFromScreen(callback, false, 0.82f, width, height, url);
    }

    public void findUrlImageFromScreen(FinderCallback callback, boolean take, int width, int height, String url) {
        findUrlImageFromScreen(callback, take, 0.82f, width, height, url);
    }

    public void findUrlImageFromScreen(FinderCallback callback, boolean take, float tolerance, int width, int height, String url) {
        int w = width * 3;
        int h = height * 3;

        if (!setTargetImage(take)) {
            callback.findFailed(2);
            return;
        }

        Glide.with(_context)
                .load(url)
                .override(355)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();

//                        Bitmap resizedBitmap = BitmapUtil.resizeBitmap(bitmap, w);
                        Bitmap resizedBitmap = BitmapUtil.resizeBitmapAspectFill(bitmap, w, h);
                        Bitmap patternBitmap = BitmapUtil.cropCenterBitmap(resizedBitmap, w, h);
//                        Bitmap patternBitmap = BitmapUtil.cropTopBitmap(resizedBitmap, w, h);
//                        Bitmap patternBitmap = BitmapUtil.resizeBitmapAspectFill(bitmap, w, h);

                        if (!setPatternImageJPEG(patternBitmap)) {
                            callback.findFailed(1);
                            return;
                        }

                        // For elapsed time checking
                        long start = System.currentTimeMillis();

                        List<Rect> rcList = new ArrayList<>();
                        Rect rt = (tolerance == 0.0f) ? _im.findMatchedSingleRect() : _im.findMatchedSingleRect(tolerance);
                        if (rt != null) {
                            rcList.add(rt);
                        }

                        // For elapsed time checking
                        long finish = System.currentTimeMillis();
                        long timeElapsed = finish - start;
                        Log.d(TAG, "Elapsed Time: " + timeElapsed);

                        callback.findSuccess(rcList);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "onLoadFailed");
                        callback.findFailed(0);
                    }
                });
    }

    public List<Rect> findResourceFromScreen(Context context, int id) {
        return findResourceFromScreen(context, id, 0.0f);
    }

    public List<Rect> findResourceFromScreen(Context context, int id, float tolerance) {
        return findResourceFromScreen(context, id, true, tolerance);
    }

    public List<Rect> findResourceFromScreen(Drawable resource) {
        return findUrlResourceFromScreen(resource, true, 0.82f);
    }

    public List<Rect> findResourceFromScreen(Drawable resource, float tolerance) {
        return findUrlResourceFromScreen(resource, true, tolerance);
    }

    public List<Rect> findResourceFromScreen(Bitmap bitmap, float tolerance) {
        return findResourceFromScreen(bitmap, true, tolerance);
    }

    public List<Rect> findUrlResourceFromScreen(Drawable resource, boolean take, float tolerance) {
        return findResourceFromScreen(((BitmapDrawable)resource).getBitmap(), take, tolerance);
    }

    public List<Rect> findResourceFromScreen(Context context, int id, boolean take, float tolerance) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);

        return findResourceFromScreen(bitmap, take, tolerance);
    }

    public List<Rect> findResourceFromScreen(Bitmap bitmap, boolean take, float tolerance) {
        if (_im == null) {
            initMatcher();
        }

        if (!setPatternImagePNG(bitmap) || !setTargetImage(take)) {
            return null;
        }

        // For elapsed time checking
        long start = System.currentTimeMillis();

        // Find matched rectangle
//        List<Rect> rcList = (tolerance == 0.0f) ? _im.findMatchedRect() : _im.findMatchedRect(tolerance);
//        if (rcList == null || rcList.size() == 0) {
//            Log.d(TAG, "Cannot find pattern image.");
//        }

        List<Rect> rcList = new ArrayList<>();
        Rect rt = (tolerance == 0.0f) ? _im.findMatchedSingleRect() : _im.findMatchedSingleRect(tolerance);
        if (rt != null) {
            rcList.add(rt);
        }

        // For elapsed time checking
        Log.d(TAG, "Elapsed Time: " + (System.currentTimeMillis() - start));

        return rcList;
    }

    public Rect findResource(Drawable resource) {
        return findResource(((BitmapDrawable)resource).getBitmap(), 0.82f);
    }

    public Rect findResource(Drawable resource, float tolerance) {
        return findResource(((BitmapDrawable)resource).getBitmap(), tolerance);
    }

    public Rect findResource(Context context, int id) {
        return findResource(context, id, 0.0f);
    }

    public Rect findResource(Context context, int id, float tolerance) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);

        return findResource(bitmap, tolerance);
    }

    public Rect findResource(Bitmap bitmap, float tolerance) {
        if (_im == null) {
            initMatcher();
        }

        if (!setPatternImagePNG(bitmap)) {
            return null;
        }

        // For elapsed time checking
        long start = System.currentTimeMillis();

        // Find matched rectangle
        Rect rt = (tolerance == 0.0f) ? _im.findMatchedSingleRect() : _im.findMatchedSingleRect(tolerance);

        // For elapsed time checking
        Log.d(TAG, "Elapsed Time: " + (System.currentTimeMillis() - start));

        return rt;
    }

    public interface FinderCallback {
        void findSuccess(List<Rect> rectList);
        void findFailed(int failCode);
    }


    private ImageFinder() {
    }

    private void initMatcher() {
        if (_im == null) {
            _im = new ImageMatcher();

            if (!_im.init()) {
                Log.d(TAG, "Cannot load OpenCV Module.");
            }
        }
    }

    private void initCapture(Context context) {
        if (_capture == null) {
            _capture = new ScreenCapture(context);
            _capture.setCallback(new ScreenCapture.Callback() {
                @Override
                public void captureSuccess(ScreenCapture capture) {
                    _mutex.threadWakeUp();
                }

                @Override
                public void captureFailed(ScreenCapture capture, int code, String message) {
                    Log.d(TAG, "캡쳐실패로 다른 방법으로 캡쳐: " +  code);
                    ScreenCaptureInjector.takeScreenshot(getFilepath());
                    _mutex.threadWakeUp();
                }
            });
        }
    }

    private boolean setPatternImage(Context context, int id) {
        return setPatternImagePNG(BitmapFactory.decodeResource(context.getResources(), id));
    }

    private boolean setPatternImagePNG(Bitmap bitmap) {
        return setPatternImage(bitmap, Bitmap.CompressFormat.PNG);
    }

    private boolean setPatternImageJPEG(Bitmap bitmap) {
        return setPatternImage(bitmap, Bitmap.CompressFormat.JPEG);
    }

    private boolean setPatternImage(Bitmap bitmap, Bitmap.CompressFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);

        // Set pattern image
        if (!_im.setPatternImage(stream.toByteArray())) {
            Log.d(TAG, "Cannot load pattern image.");
            return false;
        }

        return true;
    }

    private boolean setTargetImage(boolean take) {
        boolean setTarget = true;

        if (!take) {
            if (_im.isTargetImage()) {
                setTarget = false;
            }
        }

        if (setTarget) {
            File file = new File(take ? takeScreenshot() : getFilepath());

            if (!_im.setTargetImage(FileManager.getBytesFromFile(file))) {
                Log.d(TAG, "Cannot load target image.");
                return false;
            }
        }

        return true;
    }
}

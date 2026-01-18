package com.sec.android.app.sbrowser.engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenCapture {

    private static final String TAG = ScreenCapture.class.getSimpleName();

    public static final int REQUEST_CODE = 100;
    private static final String CAPTURE_NAME = "capture";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private final Context _context;
    private final int _width;
    private final int _height;
    private final MediaProjectionManager _projectionManager;
    private final ImageAvailableListener _listener;
    private ImageReader _imageReader;
    private MediaProjection _mediaProjection;
    private VirtualDisplay _virtualDisplay;
    private String _path;

    private PrepareCallback _prepareCallback;
    private Callback _callback;

    public ScreenCapture(Context context) {
        _context = context;

        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();

        if (wm != null) {
            wm.getDefaultDisplay().getSize(size);
        }

        _width = size.x;
        _height = size.y;
        _projectionManager = (MediaProjectionManager) _context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        _listener = new ImageAvailableListener();
        _imageReader = null;
        _mediaProjection = null;
        _virtualDisplay = null;
    }

    public void setPrepareCallback(PrepareCallback prepareCallback) {
        _prepareCallback = prepareCallback;
    }

    public void setCallback(Callback callback) {
        _callback = callback;
    }

    public void startCapture(Activity activity) {
        activity.startActivityForResult(_projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    public void stopCapture() {
        if (_virtualDisplay != null) {
            _virtualDisplay.release();
        }

        if (_imageReader != null) {
            _imageReader.setOnImageAvailableListener(null, null);
            _imageReader.close();
        }

        if (_mediaProjection != null) {
            _mediaProjection.stop();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            _mediaProjection = _projectionManager.getMediaProjection(resultCode, data);

            if (_prepareCallback != null) {
                if (_mediaProjection != null) {
                    _prepareCallback.success(this);
                } else {
                    _prepareCallback.failed(this, 1, "");
                }
            }
        }
    }

    public void captureAsync(String path) {
        _path = path;

        if (_mediaProjection != null) {
            createVirtualDisplay();
        } else {
            if (_callback != null) {
                _callback.captureFailed(this, 1, "");
            }
        }
    }

    private void createVirtualDisplay() {
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();

        if (_imageReader != null) {
            _imageReader.close();
        }

        _imageReader = ImageReader.newInstance(_width, _height, PixelFormat.RGBA_8888, 2);
        _imageReader.setOnImageAvailableListener(_listener, null);
        _virtualDisplay = _mediaProjection.createVirtualDisplay(CAPTURE_NAME, _width, _height,
                metrics.densityDpi, VIRTUAL_DISPLAY_FLAGS, _imageReader.getSurface(), null, null);
    }

    private void stop() {
        _imageReader.setOnImageAvailableListener(null, null);

        if (_virtualDisplay != null) {
            _virtualDisplay.release();
            _virtualDisplay = null;
        }
    }

    public interface PrepareCallback {
        void success(ScreenCapture capture);
        void failed(ScreenCapture capture, int code, String message);
    }

    public interface Callback {
        void captureSuccess(ScreenCapture capture);
        void captureFailed(ScreenCapture capture, int code, String message);
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            stop();

            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;
            Bitmap cropped = null;
            boolean success = false;

            try {
                image = reader.acquireLatestImage();

                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * _width;

                    // create bitmap
                    //rowStride/pixelStride
                    bitmap = Bitmap.createBitmap(_width + rowPadding / pixelStride, _height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    cropped = Bitmap.createBitmap(bitmap, 0, 0, _width, _height);
                    bitmap.recycle();

                    // write bitmap to a file
                    fos = new FileOutputStream(_path);
                    cropped.compress(CompressFormat.JPEG, 100, fos);
                    cropped.recycle();

                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (cropped != null) {
                    cropped.recycle();
                    cropped = null;
                }

                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }

                if (image != null) {
                    image.close();
                }

                if (_callback != null) {
                    if (success) {
                        _callback.captureSuccess(ScreenCapture.this);
                    } else {
                        _callback.captureFailed(ScreenCapture.this, 2, "");
                    }
                }
            }
        }
    }
}

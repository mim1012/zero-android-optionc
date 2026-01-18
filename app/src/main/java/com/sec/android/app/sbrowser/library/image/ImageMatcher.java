package com.sec.android.app.sbrowser.library.image;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class ImageMatcher {

    public class CollectedPoint {
        public int x;
        public int y;
        public float val;

        public CollectedPoint( int x, int y, float v ) {
            this.x = x;
            this.y = y;
            val = v;
        }
    }

    static final Scalar zeroScalar = new Scalar(0);

    private Mat refMat = null;
    private Mat targetMat = null;

    private final static int matchMethod = Imgproc.TM_CCOEFF_NORMED;

    /**
     * 초기화 함수
     */
    public boolean init () {
        return OpenCVLoader.initDebug();
    }

    /**
     * 소멸 함수
     */
    public void close () {
        if (refMat != null) {
            refMat.release();
            refMat = null;
        }
        if (targetMat != null) {
            targetMat.release();
            targetMat = null;
        }
    }

    /**
     * 패턴 이미지 지정
     * @param imageFileData  png, jpg 등의 이미지 파일 버퍼
     */
    public boolean setPatternImage( byte[] imageFileData ) {
        if (imageFileData == null) return false;

        refMat = Imgcodecs.imdecode(new MatOfByte(imageFileData), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        if (refMat.empty()) return false;

        return true;
    }

    /**
     * 패턴 이미지 지정
     * @param buffer RGB 형태의 메모리 버퍼
     * @param width 이미지 너비
     * @param height 이미지 높이
     */
    public boolean setPatternRGB( byte[] buffer, int width, int height ) {
        if (buffer == null) return false;

        refMat = new Mat(width, height, CvType.CV_8UC3);
        refMat.put(0, 0, buffer);
        Imgproc.cvtColor(refMat, refMat, Imgproc.COLOR_RGB2GRAY);

        if (refMat.empty()) return false;

        return true;
    }

    public boolean isTargetImage() {
        if ((targetMat != null) && (!targetMat.empty())) {
            return true;
        }

        return false;
    }

    /**
     * 대상 이미지 지정
     * @param imageFileData  png, jpg 등의 이미지 파일 버퍼
     */
    public boolean setTargetImage( byte[] imageFileData ) {
        if (imageFileData == null) return false;

        targetMat = Imgcodecs.imdecode(new MatOfByte(imageFileData), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        if (targetMat.empty()) return false;

        return true;
    }

    /**
     * 대상 이미지 지정
     * @param buffer RGB 형태의 메모리 버퍼
     * @param width 이미지 너비
     * @param height 이미지 높이
     */
    public boolean setTargetRGB( byte[] buffer, int width, int height ) {
        if (buffer == null) return false;

        targetMat = new Mat(width, height, CvType.CV_8UC3);
        targetMat.put(0, 0, buffer);
        Imgproc.cvtColor(targetMat, targetMat, Imgproc.COLOR_RGB2GRAY);

        if (targetMat.empty()) return false;

        return true;
    }

    public List<Rect> findMatchedRect() {
        return findMatchedRect(0.999f);
    }

    /**
     * 패턴 찾기 (복수개)
     * @param tolerance  검출 정확도 (기본값 0.999f)
     * @return 찾게된 사각형 정보 리스트
     */
    public List<Rect> findMatchedRect ( float tolerance ) {
        if (refMat.empty() || targetMat.empty()) return null;

        List<Rect> rcListResult = new LinkedList<>();
        Mat resultMat = new Mat(targetMat.cols() - refMat.cols() + 1, targetMat.rows() - refMat.rows() + 1, CvType.CV_32F);
        Imgproc.matchTemplate(refMat, targetMat, resultMat, matchMethod);
//        정확히 찾는 것이 목적이기 때문에 정규화나 변형을 가하지 않음. (아래 과정 X)
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//        Imgproc.threshold(result, result, tolerance, 1., Imgproc.THRESH_TOZERO);

        float[] valueData = new float[(int)resultMat.total()];
        resultMat.get(0, 0, valueData);

        int x, y, right, bottom;

        List<CollectedPoint> pointList = new LinkedList<>();
        int cCount = resultMat.cols();
        int rCount = resultMat.rows();
        for (y = 0; y < rCount; y++) {
            for (x = 0; x < cCount; x++) {
                float value = valueData[y * cCount + x];
                if (value >= tolerance) {
                    // Matched
                    ((LinkedList<CollectedPoint>) pointList).addLast(new CollectedPoint(x, y, value));
                }
            }
        }

        // 매칭률이 높은 순으로 정렬
        Collections.sort(pointList, new Comparator<CollectedPoint>() {
            @Override
            public int compare(CollectedPoint o1, CollectedPoint o2) {
                return Double.compare(o1.val, o2.val);
            }
        });

        boolean[][] invMask = new boolean[resultMat.cols()][resultMat.rows()];
        for (CollectedPoint p : pointList) {
            x = p.x;
            y = p.y;

            if (invMask[x][y]) continue;

            right = x + refMat.cols();
            bottom = y + refMat.rows();
            rcListResult.add(new Rect(x, y, right, bottom));

            // 마스크 영역을 만들어 중첩된 위치를 찾지 않도록 한다.
            DrawRectArray(invMask, x, y, refMat.cols(), refMat.rows());
        }

        return rcListResult;
    }

    public Rect findMatchedSingleRect() {
        return findMatchedSingleRect(0.999f);
    }

    /**
     * 패턴 찾기 (1개)
     * @param tolerance  검출 정확도 (기본값 0.999f)
     * @return 찾게된 사각형 정보 리스트
     */
    public Rect findMatchedSingleRect ( float tolerance ) {
        if (refMat.empty() || targetMat.empty()) return null;

        List<Rect> rcListResult = new LinkedList<>();
        Mat resultMat = new Mat(targetMat.cols() - refMat.cols() + 1, targetMat.rows() - refMat.rows() + 1, CvType.CV_32F);
        Imgproc.matchTemplate(refMat, targetMat, resultMat, matchMethod);
//        정확히 찾는 것이 목적이기 때문에 정규화나 변형을 가하지 않음. (아래 과정 X)
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//        Imgproc.threshold(result, result, tolerance, 1., Imgproc.THRESH_TOZERO);

        Core.MinMaxLocResult res = Core.minMaxLoc(resultMat);
//        Log.d("sss ", "max: " + res.maxVal + ", tol: " + tolerance);
        if (res.maxVal < tolerance) return null;

        int x = (int)res.maxLoc.x;
        int y = (int)res.maxLoc.y;

        return new Rect(x, y, x + refMat.cols(), y + refMat.rows());
    }

    private void DrawRectArray ( boolean[][] arr, int x, int y, int width, int height ) {
        int r = Math.min(x + width, arr.length);
        int b = Math.min(y + height, arr[0].length);

        int i, j;
        for (i = y; i < b; i++) {
            for (j = x; j < r; j++) {
                arr[j][i] = true;
            }
        }
    }

    /**
     * 디버깅용 이미지 그리기 (findMatchedRect... 함수 호출 한 뒤에만 사용 가능)
     * @param rcList 드로우 할 사각형 리스트 정보
     * @return 디버깅용 bitmap
     */
    public Bitmap getDebugImage ( List<Rect> rcList ) {
        Mat imgMat = targetMat;
        if (imgMat == null) return null;

        Mat outMat = new Mat();
        Imgproc.cvtColor(imgMat, outMat, Imgproc.COLOR_GRAY2RGBA);

        for (Rect rc : rcList) {
            Imgproc.rectangle(outMat, new Point(rc.left, rc.top), new Point(rc.right, rc.bottom), new Scalar(255,0,0), 2, 8, 0);
        }

        // Convert to bitmap
        Bitmap bmp = Bitmap.createBitmap(outMat.cols(), outMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outMat, bmp);

        return bmp;
    }

    private void makeGrayImage ( Mat imgMat ) {
        int nChannels = imgMat.channels();
        if (nChannels == 3) {
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
        } else if (nChannels == 4) {
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2GRAY);
        }
    }
}

/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aso.ectvoting.ui.recognition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.aso.ectvoting.R;
import com.aso.ectvoting.ui.recognition.customview.OverlayView;
import com.aso.ectvoting.ui.recognition.env.BorderedText;
import com.aso.ectvoting.ui.recognition.tflite.SimilarityClassifier;
import com.aso.ectvoting.ui.recognition.tflite.TFLiteObjectDetectionAPIModel;
import com.aso.ectvoting.ui.recognition.tracking.MultiBoxTracker;
import com.aso.ectvoting.utils.FloatArrayUtils;
import com.aso.ectvoting.utils.ImageUtils;
import com.aso.ectvoting.utils.Logger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class RecognitionActivity extends CameraActivity {
    private static final Logger LOGGER = new Logger();

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    //private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private SimilarityClassifier detector;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private boolean computingDetection = false;
    private boolean addPending = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    // Face detector
    private FaceDetector faceDetector;

    // here the preview image is drawn in portrait way
    private Bitmap portraitBmp = null;
    // here the face is cropped and drawn
    private Bitmap faceBmp = null;


    private String currentUserFace;
    private String currentUserFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setVisibility(View.INVISIBLE);

        // Real-time contour detection of multiple faces
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();


        faceDetector = FaceDetection.getClient(options);

        currentUserFace = getIntent().getStringExtra("embeedings");
        currentUserFullName = getIntent().getStringExtra("fullName");
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);


        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);


                float[] floats = FloatArrayUtils.toArray(currentUserFace);
                float[][] embeedings = new float[1][floats.length];
                embeedings[0] = floats;
                LOGGER.d("UserFace Value in onCreate : " + currentUserFace);
                LOGGER.d("UserFace Floats Array  : " + Arrays.toString(floats));

                SimilarityClassifier.Recognition rec = new SimilarityClassifier.Recognition("0", currentUserFullName, -1f, null);
                rec.setExtra(embeedings);
                detector.register(currentUserFullName,
                        rec);

        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);


        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        } else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888);

        portraitBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropW, cropH,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    if (isDebug()) {
                        tracker.drawDebug(canvas);
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;

        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        faceDetector
                .process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.size() == 0) {
                        updateResults(currTimestamp, new LinkedList<>());
                        LOGGER.e("No Faces found....");
                        return;
                    }
                    runInBackground(
                            () -> {
                                onFacesDetected(currTimestamp, faces, addPending);
                                addPending = false;
                            });
                });


    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Face Processing
    private Matrix createTransform(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

        if (applyRotation != 0) {

            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;

    }

    private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {

        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;
    }


    private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

        //Bitmap cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.0f);

        final List<SimilarityClassifier.Recognition> mappedRecognitions =
                new LinkedList<>();

        // Note this can be done only once
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap, transform, null);

        final Canvas cvFace = new Canvas(faceBmp);

        for (Face face : faces) {

            LOGGER.i("FACE" + face.toString());
            LOGGER.i("Running detection on face " + currTimestamp);

            final RectF boundingBox = new RectF(face.getBoundingBox());

            // maps crop coordinates to original
            cropToFrameTransform.mapRect(boundingBox);

            // maps original coordinates to portrait coordinates
            RectF faceBB = new RectF(boundingBox);
            transform.mapRect(faceBB);

            // translates portrait to origin and scales to fit input inference size
            //cv.drawRect(faceBB, paint);
            float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
            float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
            Matrix matrix = new Matrix();
            matrix.postTranslate(-faceBB.left, -faceBB.top);
            matrix.postScale(sx, sy);

            cvFace.drawBitmap(portraitBmp, matrix, null);

            String label = "";
            float confidence = -1f;
            int color = Color.BLUE;
            Object extra = null;
            Bitmap crop = null;

            if (add) {
                crop = Bitmap.createBitmap(portraitBmp,
                        (int) faceBB.left,
                        (int) faceBB.top,
                        (int) faceBB.width(),
                        (int) faceBB.height());
            }


            if (crop != null) {
                LOGGER.e("Size of the face " + crop.getByteCount());
                //ImageUtils.getBase64StringFromBitmap(crop);
            }

            final long startTime = SystemClock.uptimeMillis();
            final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, add);

            if (resultsAux.size() > 0) {

                SimilarityClassifier.Recognition result = resultsAux.get(0);

                extra = result.getExtra();
                if (extra != null) {
                    LOGGER.i("embeeding retrieved " + extra.toString());
                }

                float conf = result.getDistance();
                if (conf < 1.0f) {

                    confidence = conf;
                    label = result.getTitle();
                    if (result.getId().equals("0")) {
                        color = Color.GREEN;
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        color = Color.RED;
                        label = "Face not recognized";
                    }
                }

            }

            if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {

                // camera is frontal so the image is flipped horizontally
                // flips horizontally
                Matrix flip = new Matrix();
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                } else {
                    flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                }
                //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
                flip.mapRect(boundingBox);

            }

            final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                    "0", label, confidence, boundingBox);

            result.setColor(color);
            result.setLocation(boundingBox);
            result.setExtra(extra);
            result.setCrop(crop);
            mappedRecognitions.add(result);

        }

        updateResults(currTimestamp, mappedRecognitions);

    }
}

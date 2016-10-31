package com.example.intern.ptp.views.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.DemoUtil;

import java.util.HashMap;
import java.util.List;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class LocatorMapPhotoView extends PhotoView implements PhotoViewAttacher.OnMatrixChangedListener {

    private RectF displayRect;

    private HashMap<String, ResidentMarker> residentMarker;

    private String followThisResident;

    private float scale;
    private double bottomSideDisplayInch;

    private static final float SCALED_CIRCLE_SIZE = 13f;
    private static final float SCALED_PROFILE_IMAGE_SIZE = 18f;
    private static final float SCALED_TEXT_SIZE = 24f;
    private static final float MAX_WITHIN_TOUCH_DISTANCE = 50f;
    private static final double MIN_RESIDENT_IMAGE_INCHES = 5d;
    private static final int ANIMATION_DURATION = 1000;

    private Paint circlePaint;
    private Paint textPaint;

    private boolean isReady = false;

    public LocatorMapPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        isInEditMode();

        residentMarker = new HashMap<>();

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        double density = metrics.density * 160;
        double x = Math.pow(metrics.widthPixels / density, 2);
        double y = Math.pow(metrics.heightPixels / density, 2);

        int orientation = getContext().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            bottomSideDisplayInch = Math.sqrt(Math.min(x, y));
        } else {
            bottomSideDisplayInch = Math.sqrt(Math.max(x, y));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady) {
            return;
        }

        if (followThisResident != null && residentMarker.containsKey(followThisResident)) {

            ResidentMarker marker = residentMarker.get(followThisResident);

            float residentX = convertResidentX(marker.x, scale);
            float residentY = convertResidentY(marker.y, scale);



        }

        scale = calculateScale();

        for (final ResidentMarker marker : residentMarker.values()) {

            animateMarker(marker);

            if (marker.isResident) {
                drawResidentIndicator(canvas, marker);
            } else {
                drawNurseIndicator(canvas, marker);
            }
        }

        for (final ResidentMarker marker : residentMarker.values()) {
            if (marker.isResident) {
                drawResidentName(canvas, marker);
            }
        }

        this.postInvalidateDelayed(1000 / 60);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        displayRect = getDisplayRect();
    }

    @Override
    public void onMatrixChanged(RectF rect) {
        displayRect = rect;
        invalidate();
    }

    public void setResidents(List<Resident> residents) {

        for (Resident resident : residents) {
            ResidentMarker marker = convertResidentToMarker(resident);
            residentMarker.put(resident.getId(), marker);
        }

        invalidate();
    }

    public void setFollowResident(Resident resident) {
        followThisResident = resident.getId();


        invalidate();
    }

    private ResidentMarker convertResidentToMarker(Resident resident) {
        ResidentMarker marker = residentMarker.get(resident.getId());

        if (marker == null) {
            marker = new ResidentMarker();
            marker.x = Integer.parseInt(resident.getPixelx());
            marker.y = Integer.parseInt(resident.getPixely());
        }

        marker.color = Integer.parseInt(resident.getColor());
        marker.displayName = resident.getFirstname();

        if (resident.isResident()) {
            Drawable residentDrawable = DemoUtil.getResidentProfileDrawable(getContext(), resident.getId());
            marker.image = ((BitmapDrawable) residentDrawable).getBitmap();
        } else {
            marker.isResident = false;
        }

        marker.destinationX = Integer.parseInt(resident.getPixelx());
        marker.destinationY = Integer.parseInt(resident.getPixely());
        marker.originX = marker.x;
        marker.originY = marker.y;
        marker.timestamp = System.currentTimeMillis();

        return marker;
    }

    private float calculateScale() {
        Bitmap b = ((BitmapDrawable) getDrawable()).getBitmap();
        float originalWidth = b.getWidth();

        float scaledWidth = displayRect.right - displayRect.left;
        return scaledWidth / originalWidth;
    }

    private float convertResidentX(float x, float scale) {
        return displayRect.left + (x * scale);
    }

    private float convertResidentY(float y, float scale) {
        return displayRect.top + (y * scale);
    }

    private float calculateTextX(float x, float textWidth) {
        return x - (textWidth / 2.f);
    }

    private float calculateTextY(float y, float textSize) {
        return y - (0.9f * textSize);
    }

    private void animateMarker(ResidentMarker marker) {

        long timeElapsed = System.currentTimeMillis() - marker.timestamp;

        if (timeElapsed < ANIMATION_DURATION) {
            float animationProgressPercentage = (float) timeElapsed / ANIMATION_DURATION;
            marker.x = (int) (marker.originX + (animationProgressPercentage * (marker.destinationX - marker.originX)));
            marker.y = (int) (marker.originY + (animationProgressPercentage * (marker.destinationY - marker.originY)));
        }
    }

    private void drawResidentIndicator(Canvas canvas, ResidentMarker marker) {
        circlePaint.setColor(marker.color);

        float residentX = convertResidentX(marker.x, scale);
        float residentY = convertResidentY(marker.y, scale);

        if (bottomSideDisplayInch * scale > MIN_RESIDENT_IMAGE_INCHES) {
            int imageRadius = (int) (SCALED_PROFILE_IMAGE_SIZE * scale);

            Bitmap croppedBitmap = getCroppedBitmap(marker.image, imageRadius * 2, imageRadius * 2);

            canvas.drawBitmap(croppedBitmap, residentX - imageRadius, residentY - imageRadius, circlePaint);

            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(3 * scale);
            canvas.drawCircle(residentX, residentY, SCALED_PROFILE_IMAGE_SIZE * scale, circlePaint);
        } else {
            int circleRadius = (int) (SCALED_CIRCLE_SIZE * scale);

            circlePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(residentX, residentY, circleRadius, circlePaint);
        }
    }

    private void drawNurseIndicator(Canvas canvas, ResidentMarker marker) {
        circlePaint.setColor(marker.color);

        float residentX = convertResidentX(marker.x, scale);
        float residentY = convertResidentY(marker.y, scale);

        int circleRadius = (int) (SCALED_CIRCLE_SIZE * scale);

        circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(residentX, residentY, circleRadius, circlePaint);
    }

    private void drawResidentName(Canvas canvas, ResidentMarker marker) {
        float residentX = convertResidentX(marker.x, scale);
        float residentY = convertResidentY(marker.y, scale);

        textPaint.setTextSize(SCALED_TEXT_SIZE * scale);
        float residentNameX = calculateTextX(residentX, textPaint.measureText(marker.displayName));
        float residentNameY = calculateTextY(residentY, textPaint.getTextSize());

        textPaint.setColor(Color.GRAY);
        canvas.drawText(marker.displayName, residentNameX + 1, residentNameY, textPaint);
        textPaint.setColor(marker.color);
        canvas.drawText(marker.displayName, residentNameX, residentNameY, textPaint);
    }

    public Resident getTouchedResident(PointF point) {
        float closestDistance = Float.MAX_VALUE;
        Resident closestResident = null;

        for (ResidentMarker marker : residentMarker.values()) {
            float residentX = convertResidentX(marker.x, scale);
            float residentY = convertResidentY(marker.y, scale);

            float distance = (float) (Math.pow(point.x - residentX, 2.d) + Math.pow(point.y - residentY, 2.d));

            if (closestDistance > distance) {
                closestDistance = distance;
                closestResident = marker.resident;
            }
        }

        if (Math.sqrt(closestDistance) > MAX_WITHIN_TOUCH_DISTANCE) {
            return null;
        } else {
            return closestResident;
        }
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap, int width, int height) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return Bitmap.createScaledBitmap(output, width, height, false);
    }

    public void ready() {
        isReady = true;
        displayRect = getDisplayRect();
    }

    private class ResidentMarker {
        Resident resident;

        int destinationX, destinationY;
        int originX, originY;
        int x, y;
        int color;
        String displayName;
        Bitmap image;
        boolean isResident = true;
        long timestamp;
    }
}

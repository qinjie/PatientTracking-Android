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

import com.example.intern.library.PhotoView;
import com.example.intern.library.PhotoViewAttacher;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.DemoUtil;

import java.util.ArrayList;
import java.util.List;

public class LocatorMapPhotoView extends PhotoView implements PhotoViewAttacher.OnMatrixChangedListener {

    private RectF displayRect;
    private List<Resident> residents;

    private float originalWidth;
    private float scale;
    private double screenWidth;

    private static final float SCALED_CIRCLE_SIZE = 13f;
    private static final float SCALED_PROFILE_IMAGE_SIZE = 18f;
    private static final float SCALED_TEXT_SIZE = 24f;
    private static final float MAX_WITHIN_TOUCH_DISTANCE = 50f;

    private static final double MIN_RESIDENT_IMAGE_INCHES = 5d;

    private Paint circlePaint;
    private Paint textPaint;

    public LocatorMapPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        residents = new ArrayList<>();

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        displayRect = getDisplayRect();

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        double density = metrics.density * 160;
        double x = Math.pow(metrics.widthPixels / density, 2);
        double y = Math.pow(metrics.heightPixels / density, 2);
        originalWidth = metrics.widthPixels;

        int orientation = getContext().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = Math.sqrt(Math.min(x,y));
        } else {
            screenWidth = Math.sqrt(Math.max(x,y));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        displayRect = getDisplayRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        scale = calculateScale();

        for (final Resident resident : residents) {
            drawResidentIndicator(canvas, resident);
        }

        for (final Resident resident : residents) {
            if (!resident.isNurse()) {
                drawResidentName(canvas, resident);
            }
        }
    }

    @Override
    public void onMatrixChanged(RectF rect) {
        displayRect = rect;
        invalidate();
    }

    public void setResidents(List<Resident> residents) {
        this.residents = residents;
        invalidate();
    }

    private float calculateScale() {
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

    private void drawResidentIndicator(Canvas canvas, Resident resident) {
        int color = Integer.parseInt(resident.getColor());
        circlePaint.setColor(color);

        int x = Integer.parseInt(resident.getPixelx());
        int y = Integer.parseInt(resident.getPixely());
        float residentX = convertResidentX(x, scale);
        float residentY = convertResidentY(y, scale);

        if (screenWidth * scale > MIN_RESIDENT_IMAGE_INCHES) {
            int imageRadius = (int) (SCALED_PROFILE_IMAGE_SIZE * scale);

            Drawable residentDrawable = DemoUtil.getResidentProfileDrawable(getContext(), resident.getId());
            Bitmap bitmap = ((BitmapDrawable) residentDrawable).getBitmap();
            Bitmap croppedBitmap = getCroppedBitmap(bitmap, imageRadius * 2, imageRadius * 2);

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

    private void drawResidentName(Canvas canvas, Resident resident) {
        int x = Integer.parseInt(resident.getPixelx());
        int y = Integer.parseInt(resident.getPixely());
        float residentX = convertResidentX(x, scale);
        float residentY = convertResidentY(y, scale);

        String text = resident.getFirstname();

        textPaint.setTextSize(SCALED_TEXT_SIZE * scale);
        float residentNameX = calculateTextX(residentX, textPaint.measureText(text));
        float residentNameY = calculateTextY(residentY, textPaint.getTextSize());

        textPaint.setColor(Color.GRAY);
        canvas.drawText(text, residentNameX + 1, residentNameY, textPaint);
        int color = Integer.parseInt(resident.getColor());
        textPaint.setColor(color);
        canvas.drawText(text, residentNameX, residentNameY, textPaint);
    }

    public Resident getTouchedResident(PointF point) {
        float closestDistance = Float.MAX_VALUE;
        Resident closestResident = null;

        for (Resident resident : residents) {
            int x = Integer.parseInt(resident.getPixelx());
            int y = Integer.parseInt(resident.getPixely());
            float residentX = convertResidentX(x, scale);
            float residentY = convertResidentY(y, scale);

            float distance = (float) (Math.pow(point.x - residentX, 2.d) + Math.pow(point.y - residentY, 2.d));

            if (closestDistance > distance) {
                closestDistance = distance;
                closestResident = resident;
            }
        }

        if (Math.sqrt(closestDistance) > MAX_WITHIN_TOUCH_DISTANCE) {
            return null;
        } else {
            return closestResident;
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap, int width, int height) {
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
}

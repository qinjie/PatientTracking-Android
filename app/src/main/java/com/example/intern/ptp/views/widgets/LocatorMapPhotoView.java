package com.example.intern.ptp.views.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.example.intern.library.PhotoView;
import com.example.intern.library.PhotoViewAttacher;
import com.example.intern.ptp.network.models.Resident;

import java.util.ArrayList;
import java.util.List;

public class LocatorMapPhotoView extends PhotoView implements PhotoViewAttacher.OnMatrixChangedListener {

    private RectF displayRect;
    private List<Resident> residents;

    private float originalWidth;
    private float scale;

    private static final float SCALED_CIRCLE_SIZE = 12;
    private static final float SCALED_TEXT_SIZE = 32;
    private static final float MAX_WITHIN_TOUCH_DISTANCE = 50;

    private Paint circlePaint;
    private Paint textPaint;

    private Resident touchedResident;

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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        displayRect = getDisplayRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateScaleFactor();

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
        float scaledImageWidth = displayRect.right - displayRect.left;

        return scaledImageWidth / originalWidth;
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
        return y - (0.75f * textSize);
    }

    private void updateScaleFactor() {
        Bitmap b = ((BitmapDrawable) getDrawable()).getBitmap();
        originalWidth = b.getWidth();
        scale = calculateScale();
    }

    private void drawResidentIndicator(Canvas canvas, Resident resident) {
        int color = Integer.parseInt(resident.getColor());

        if (resident == touchedResident) {
            circlePaint.setColor(Color.BLACK);
        } else {
            circlePaint.setColor(color);
        }

        int x = Integer.parseInt(resident.getPixelx());
        int y = Integer.parseInt(resident.getPixely());
        float residentX = convertResidentX(x, scale);
        float residentY = convertResidentY(y, scale);

        circlePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(residentX, residentY, SCALED_CIRCLE_SIZE * scale, circlePaint);
        circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(residentX, residentY, (SCALED_CIRCLE_SIZE - 2) * scale, circlePaint);
    }

    private void drawResidentName(Canvas canvas, Resident resident) {
        int x = Integer.parseInt(resident.getPixelx());
        int y = Integer.parseInt(resident.getPixely());
        float residentX = convertResidentX(x, scale);
        float residentY = convertResidentY(y, scale);

        String text = resident.getFirstname();

        textPaint.setTextSize(SCALED_TEXT_SIZE * (scale * 0.75f));
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
}

package com.achep.widget.jellyclock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.text.format.Time;

public class AnalogClock {

	private float mPropSize;
	private float mPropShadowRadius;
	private float mPropBorderRadius;
	private float mPropBorderWidth;
	private float mPropHourHeight;
	private float mPropHourHeightNegative;
	private float mPropMinuteHeight;
	private float mPropMinuteHeightNegative;
	private float mPropHandsWidth;

	private Time mTime;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mPaint;

	public AnalogClock(Resources r) {
		loadDimensions(r);

		mTime = new Time();
		mBitmap = Bitmap.createBitmap(Math.round(mPropSize),
				Math.round(mPropSize), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(r.getColor(R.color.widget_stroke_color));
		mPaint.setShadowLayer(mPropShadowRadius, 0, 0,
				r.getColor(R.color.widget_shadow_color));
	}

	public Bitmap draw() {
		float center = mPropSize / 2;
		mTime.setToNow();

		mCanvas.drawColor(0, Mode.CLEAR);

		// Draw clock circle
		mPaint.setStrokeWidth(mPropBorderWidth);
		mCanvas.drawCircle(center, center, mPropBorderRadius, mPaint);

		// Hands
		mPaint.setStrokeWidth(mPropHandsWidth);

		// Draw hour hand
		mCanvas.save();
		mCanvas.rotate(mTime.hour * 30 + mTime.minute / 2, center, center);
		mCanvas.drawLine(center, center - mPropHourHeight, center, center
				+ mPropHourHeightNegative, mPaint);
		mCanvas.restore();

		// Draw minute hand
		mCanvas.save();
		mCanvas.rotate(mTime.minute * 6, center, center);
		mCanvas.drawLine(center, center - mPropMinuteHeight, center, center
				+ mPropMinuteHeightNegative, mPaint);
		mCanvas.restore();

		return mBitmap;
	}

	private void loadDimensions(Resources r) {
		mPropSize = r.getDimension(R.dimen.widget_analog_size);
		mPropShadowRadius = r.getDimension(R.dimen.widget_analog_shadow_radius);
		mPropBorderRadius = r.getDimension(R.dimen.widget_analog_border_radius);
		mPropBorderWidth = r.getDimension(R.dimen.widget_analog_border_width);
		mPropHourHeight = r.getDimension(R.dimen.widget_analog_hour_height);
		mPropHourHeightNegative = r
				.getDimension(R.dimen.widget_analog_hour_height_negative);
		mPropMinuteHeight = r.getDimension(R.dimen.widget_analog_minute_height);
		mPropMinuteHeightNegative = r
				.getDimension(R.dimen.widget_analog_minute_height_negative);
		mPropHandsWidth = r.getDimension(R.dimen.widget_analog_hands_width);
	}
}

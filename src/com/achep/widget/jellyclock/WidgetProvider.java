/*
 * Copyright (C) 2012 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.achep.widget.jellyclock;

import com.achep.widget.jellyclock.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Simple widget to show analog clock.
 */
public class WidgetProvider extends AppWidgetProvider {

	private Intent mUpdateService;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				AppWidgetManager.ACTION_APPWIDGET_DISABLED)) {
			context.stopService(mUpdateService);
		} else {
			if (mUpdateService == null)
				mUpdateService = new Intent(context, UpdateService.class);

			context.startService(mUpdateService);
		}
	}

	public static class UpdateService extends Service {

		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

			private boolean isScreenOn = true;

			@Override
			public void onReceive(Context context, Intent intent) {
				String key = intent.getAction();
				if (key.equals(Intent.ACTION_SCREEN_ON)) {
					isScreenOn = true;
					updateWidget();
				} else if (key.equals(Intent.ACTION_SCREEN_OFF)) {
					isScreenOn = false;
				} else if (isScreenOn) {
					updateWidget();
				}
			}

		};

		private RemoteViews mRemoteViews;
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Paint mPaint;

		private float mPropBitmapSize;
		private float mPropShadowRadius;
		private float mPropCircleStrokeWidth;
		private float mPropCircleRadius;
		private float mPropHandsStrokeWidth;
		private float mPropHandHourHeight;
		private float mPropHandHourHeightOver;
		private float mPropHandMinuteHeight;
		private float mPropHandMinuteHeightOver;
		private int mPropStrokeColor;
		private int mPropShadowColor;

		@Override
		public void onCreate() {
			mRemoteViews = new RemoteViews(getPackageName(),
					R.layout.analog_appwidget);
			mRemoteViews.setOnClickPendingIntent(
					R.id.analog_appwidget,
					PendingIntent.getActivity(this, 0,
							Utils.getAlarmIntent(this), 0));

			loadProps();

			mBitmap = Bitmap.createBitmap(Math.round(mPropBitmapSize),
					Math.round(mPropBitmapSize), Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(mPropStrokeColor);
			mPaint.setShadowLayer(mPropShadowRadius, 0, 0, mPropShadowColor);

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			registerReceiver(mReceiver, filter);
		}

		@Override
		public void onDestroy() {
			unregisterReceiver(mReceiver);
		}

		@Override
		public void onStart(Intent intent, int startId) {
			updateWidget();
		}

		private void loadProps() {
			Resources r = getResources();

			mPropBitmapSize = r.getDimension(R.dimen.widget_bitmapsize);
			mPropShadowRadius = r.getDimension(R.dimen.widget_shadow_radius);

			// Circle
			mPropCircleStrokeWidth = r
					.getDimension(R.dimen.widget_circle_stroke_width);
			mPropCircleRadius = r.getDimension(R.dimen.widget_circle_radius);

			// Hands
			mPropHandsStrokeWidth = r
					.getDimension(R.dimen.widget_hands_stroke_width);

			// Hour
			mPropHandHourHeight = r.getDimension(R.dimen.widget_hand_hour);
			mPropHandHourHeightOver = r
					.getDimension(R.dimen.widget_hand_hour_over);

			// Minute
			mPropHandMinuteHeight = r.getDimension(R.dimen.widget_hand_minute);
			mPropHandMinuteHeightOver = r
					.getDimension(R.dimen.widget_hand_minute_over);

			// Colors
			mPropShadowColor = r.getColor(R.color.widget_shadow_color);
			mPropStrokeColor = r.getColor(R.color.widget_stroke_color);
		}

		private void updateWidget() {
			float center = mPropBitmapSize / 2;
			Time time = new Time();
			time.setToNow();

			mCanvas.drawColor(0, Mode.CLEAR);

			// Draw clock circle
			mPaint.setStrokeWidth(mPropCircleStrokeWidth);
			mCanvas.drawCircle(center, center, mPropCircleRadius, mPaint);

			// Hands
			mPaint.setStrokeWidth(mPropHandsStrokeWidth);

			// Draw hour hand
			mCanvas.save();
			mCanvas.rotate(time.hour * 30 + time.minute / 2, center, center);
			mCanvas.drawLine(center, center - mPropHandHourHeight, center,
					center + mPropHandHourHeightOver, mPaint);
			mCanvas.restore();

			// Draw minute hand
			mCanvas.save();
			mCanvas.rotate(time.minute * 6, center, center);
			mCanvas.drawLine(center, center - mPropHandMinuteHeight, center,
					center + mPropHandMinuteHeightOver, mPaint);
			mCanvas.restore();

			// Apply changes
			mRemoteViews.setImageViewBitmap(R.id.analog_appwidget, mBitmap);
			AppWidgetManager.getInstance(this)
					.updateAppWidget(
							new ComponentName(this, WidgetProvider.class),
							mRemoteViews);
		}

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}

	}
}

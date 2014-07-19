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
import android.os.IBinder;
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
				mUpdateService = new Intent(); //ensure this isn't null to prevent NPE
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
		private AnalogClock mAnalogClock;

		@Override
		public void onCreate() {
			mRemoteViews = new RemoteViews(getPackageName(),
					R.layout.analog_appwidget);
			mRemoteViews.setOnClickPendingIntent(
					R.id.analog_appwidget,
					PendingIntent.getActivity(this, 0,
							Utils.getAlarmIntent(this), 0));

			mAnalogClock = new AnalogClock(getResources());

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

		private void updateWidget() {
			mRemoteViews.setImageViewBitmap(R.id.analog_appwidget,
					mAnalogClock.draw());
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

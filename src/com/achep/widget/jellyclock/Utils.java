package com.achep.widget.jellyclock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {
	public static Intent getAlarmIntent(Context context) {
		PackageManager packageManager = context.getPackageManager();
		Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN)
				.addCategory(Intent.CATEGORY_LAUNCHER);
		String clockImpls[][] = {
				{ "com.htc.android.worldclock",
						"com.htc.android.worldclock.WorldClockTabControl" },
				{ "com.android.deskclock", "com.android.deskclock.AlarmClock" },
				{ "com.google.android.deskclock",
						"com.android.deskclock.DeskClock" },
				{ "com.motorola.blur.alarmclock",
						"com.motorola.blur.alarmclock.AlarmClock" },
				{ "com.sec.android.app.clockpackage",
						"com.sec.android.app.clockpackage.ClockPackage" } };
		boolean foundClockImpl = false;
		for (int i = 0; i < clockImpls.length; i++) {
			String packageName = clockImpls[i][0];
			String className = clockImpls[i][1];
			try {
				ComponentName cn = new ComponentName(packageName, className);
				packageManager
						.getActivityInfo(cn, PackageManager.GET_META_DATA);
				alarmClockIntent.setComponent(cn);
				foundClockImpl = true;
			} catch (NameNotFoundException nf) {
			}
		}
		return (foundClockImpl) ? alarmClockIntent : null;
	}
}

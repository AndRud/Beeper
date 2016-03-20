package com.example.beeper;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.beeper.MainActivity;

public class BeepWidget extends AppWidgetProvider{

	final static String LOG_TAG = "myLogs";
	private final static String ACTION_BEEP = "com.example.beeper.beep";
	
	final static String ACTION_BEEP_UPDATE = "com.example.beeper.beep_update";
	
	private boolean isBeep = false;
	private ActivityManager manager;
	
	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		isBeep = MainActivity.isBeepServiceRunning(BeeperService.class, manager);
		for (int i : appWidgetIds)
			updateWidget(context, appWidgetManager, i, isBeep);
	}
	
	private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID, boolean isBeep){
		RemoteViews widetView = new RemoteViews(context.getPackageName(), R.layout.beep_widget);
		Intent beepIntent = new Intent(context, BeepWidget.class);
		
		if (isBeep){
			widetView.setTextViewText(R.id.tvBeep, context.getResources().getString(R.string.beep_on));
		} else {
			widetView.setTextViewText(R.id.tvBeep, context.getResources().getString(R.string.beep_off));
		}
		
		beepIntent.setAction(ACTION_BEEP);
		beepIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		PendingIntent beepPIntent = PendingIntent.getBroadcast(context, widgetID, beepIntent, 0);
		widetView.setOnClickPendingIntent(R.id.tvBeep, beepPIntent);
		
		appWidgetManager.updateAppWidget(widgetID, widetView);
	}
	
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
		Log.d(LOG_TAG, "onReceive");
		if (intent.getAction().equalsIgnoreCase(ACTION_BEEP)){
			int beepAppWidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
			Bundle extras = intent.getExtras();
			if (extras != null){
				beepAppWidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			} 
			if (beepAppWidgetID != AppWidgetManager.INVALID_APPWIDGET_ID){
				manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				isBeep = MainActivity.isBeepServiceRunning(BeeperService.class, manager);
				if (isBeep){
					context.stopService(new Intent(context, BeeperService.class));
				} else {
					context.startService(new Intent(context, BeeperService.class));
				}
				updateWidget(context, AppWidgetManager.getInstance(context), beepAppWidgetID, !isBeep);
			}
		}
		if (intent.getAction().equalsIgnoreCase(ACTION_BEEP_UPDATE)){
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
			ComponentName thisWidget = new ComponentName(context.getApplicationContext(), BeepWidget.class);
			int[] beepAppWidgetID = appWidgetManager.getAppWidgetIds(thisWidget);
			manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			isBeep = MainActivity.isBeepServiceRunning(BeeperService.class, manager);
			for (int i : beepAppWidgetID){
				updateWidget(context, AppWidgetManager.getInstance(context), i, isBeep);
			}
		}
	}
}

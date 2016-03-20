package com.example.beeper;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class BeeperService extends Service implements OnLoadCompleteListener{

	private final String PREF_TIME_TO_BEEP = "timeToBeep";
	private final String PREF_COUNT_BEEP = "countBeep";
	private final String PREF_SOUND_BEEP = "listSounds";
	
	private final String FOLDER_NAME = "raw";
	
	final String LOG_TAG = "myLogs";
	final int MAX_STREAMS = 1;
	final int NOTIFIG_ID = 1;
	
	private SoundPool spBeep;
	private int soundID;
	private TimerTask taskBeep;
	private Timer timerBeep;
	
	private NotificationManager nmBeep;
	
	private PowerManager powerManager;
	private WakeLock wakeLock;
	private AudioManager audioManager;
	private AFListener afListener;

	public void onCreate(){
		super.onCreate();
		initBeeper();
	}
	
	private void initBeeper(){
		spBeep = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		spBeep.setOnLoadCompleteListener(this);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String soundBeep = sp.getString(PREF_SOUND_BEEP, "hangouts_message");
		soundID = spBeep.load(getApplicationContext(), getResID(getApplicationContext(), FOLDER_NAME, soundBeep), 1);
		timerBeep = new Timer();
		nmBeep = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		afListenerBeep = new AFListener("Sound");
	}
	
	private int getResID(Context context, String folderName, String resName){
		return context.getResources().getIdentifier(folderName + "/" + resName, folderName, context.getPackageName());
	}
	
	private void playBeep(long timeToBeep, final int countBeep){
		if (taskBeep != null) taskBeep.cancel();
		if (timeToBeep <= 0){
			Toast.makeText(getApplicationContext(), "Illegal time: " + timeToBeep, Toast.LENGTH_SHORT).show();
			stopSelf();
		}
		if (countBeep <= 0){
			Toast.makeText(getApplicationContext(), "Illegal count Beep: " + countBeep, Toast.LENGTH_SHORT).show();
			stopSelf();
		}
		taskBeep = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int reguestResult = audioManager.requestAudioFocus(afListenerBeep,
						AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
				spBeep.play(soundID, 1, 1, 0, countBeep - 1, 1);
				audioManager.abandonAudioFocus(afListenerBeep);
			}
		};
		try {
			timerBeep.schedule(taskBeep, 0, timeToBeep * 1000);
			sendBeepStateWidget();
			sendNotif();
			
		} catch (IllegalArgumentException e) {
			Toast.makeText(getApplicationContext(), "Illegal time " + timeToBeep, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			stopSelf();
		}
	}
	
	private void sendBeepStateWidget(){
		Intent broadcastBeepIntent = new Intent();
		broadcastBeepIntent.setAction(BeepWidget.ACTION_BEEP_UPDATE);
		sendBroadcast(broadcastBeepIntent);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		
		return START_STICKY;
	}
	
	private void sendNotif(){
		Notification ntBeep = new Notification(R.drawable.ic_launcher, getString(R.string.beeper), System.currentTimeMillis());
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pBeep = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		ntBeep.setLatestEventInfo(getApplicationContext(), "Beeper", "Beep On", pBeep);
		
		ntBeep.flags |= Notification.FLAG_NO_CLEAR;
		nmBeep.notify(NOTIFIG_ID, ntBeep);
	}
	
	public void onDestroy(){
		super.onDestroy();
		if (taskBeep != null) taskBeep.cancel();
		if(spBeep != null){
			spBeep.release();
			spBeep = null;
		}
		if (afListenerBeep != null)
			audioManager.abandonAudioFocus(afListenerBeep);
		nmBeep.cancel(NOTIFIG_ID);
		sendBeepStateWidget();
		wakeLock.release();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		// TODO Auto-generated method stub
		int timeToBeep = getSomePref(getApplicationContext(), PREF_TIME_TO_BEEP, 5);
		int countBeep = getSomePref(getApplicationContext(), PREF_COUNT_BEEP, 1);
		playBeep(timeToBeep, countBeep);
	}
	
	private static int getSomePref(Context context, String key, int defValue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String value = "";
		try {
			value = sp.getString(key, "");
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return value == "" ? defValue : Integer.valueOf(value);
	}

	class AFListener implements OnAudioFocusChangeListener {

		String label = "";

		public AFListener(String label) {
			this.label = label;
		}

		@Override
		public void onAudioFocusChange(int focusChange) {
			String event = "";
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_LOSS:
					event = "AUDIOFOCUS_LOSS";
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					event = "AUDIOFOCUS_LOSS_TRANSIENT";
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					event = "AUDIOFOCUS_GAIN";
					break;
			}
			Log.d(LOG_TAG, label + " onAudioFocusChange: " + event);
		}
	}
}

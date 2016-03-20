package com.example.beeper;

import android.app.Activity;
import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity; 

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;

import ar.com.daidalos.afiledialog.FileChooserActivity;
import ar.com.daidalos.afiledialog.FileChooserDialog;

public class MainActivity extends ActionBarActivity {

	final String LOG_TAG = "myLogs";
	
	private ToggleButton btnBeep;
	public boolean isBeep = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		btnBeep = (ToggleButton) findViewById(R.id.btnBeep);
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		isBeep = isBeepServiceRunning(BeeperService.class, manager);
		btnBeep.setChecked(isBeep);
	}
	
	public void onClickBeep(View view){
		/*Intent testIntent = new Intent(Intent.ACTION_GET_CONTENT);
		testIntent.setType("file/*");
		startActivityForResult(testIntent, 1);
		FileChooserDialog fileChooserDialog = new FileChooserDialog(this);
		fileChooserDialog.show();*/
		/*Intent intent = new Intent(this, FileChooserActivity.class);
		startActivityForResult(intent, 1);*/
		isBeep = !isBeep;
		if (isBeep){
			Intent intentBeep = new Intent(this, BeeperService.class);
			startService(intentBeep);
		} else {
			Intent intentBeep = new Intent(this, BeeperService.class);
			stopService(intentBeep);
		}
	}
		
	public static boolean isBeepServiceRunning(Class<?> serviceClass, ActivityManager manager){
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceClass.getName().equals(service.service.getClassName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, PrefBeep.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View view){
		Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == Activity.RESULT_OK) {
			Log.d(LOG_TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode);

			boolean fileCreated = false;
			String filePath = "";

			Bundle bundle = data.getExtras();
			if(bundle != null)
			{
				if(bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
					fileCreated = true;
					File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
					String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
					filePath = folder.getAbsolutePath() + "/" + name;
				} else {
					fileCreated = false;
					File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
					filePath = file.getAbsolutePath();
				}
			}

			String message = fileCreated? "File created" : "File opened";
			message += ": " + filePath;
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
}

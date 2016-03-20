package com.example.beeper;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity; 

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {

	final String LOG_TAG = "myLogs";
	
	private ToggleButton btnBeep;
	public boolean isBeep = false;
	private Button btnTest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_main);
		
		btnBeep = (ToggleButton) findViewById(R.id.btnBeep);
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		isBeep = isBeepServiceRunning(BeeperService.class, manager);
		btnBeep.setChecked(isBeep);
		btnTest = (Button) findViewById(R.id.btnTest);
	}
	
	public void onClickBeep(View view){
		isBeep = !isBeep;
		Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();
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
}

package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.artifex.mupdfdemo.R;

public class SettingsActivity extends Activity {
	private EditText musername;
	private EditText mpassword;
	private EditText maddress;
	private EditText mport;
	private Button mcancel;
	private Button msave;
	
	public static String PREFFILE = "Student.Settings";
	public static String PREFUSER = "FTPusername";
	public static String PREFPASS = "FTPpassword";
	public static String PREFADDR = "FTPaddress";
	public static String PREFPORT = "FTPport";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout layout = (LinearLayout) LinearLayout.inflate(this, R.layout.settingsactivity, null);

		musername = (EditText) layout.findViewById(R.id.username);
		mpassword = (EditText) layout.findViewById(R.id.password);
		maddress = (EditText) layout.findViewById(R.id.address);
		mport = (EditText) layout.findViewById(R.id.port);
		
		mcancel = (Button) layout.findViewById(R.id.cancelSettings);
		msave = (Button) layout.findViewById(R.id.saveSettings);
		
		SharedPreferences prefs = getSharedPreferences(PREFFILE, 0);
		musername.setText(prefs.getString(PREFUSER, ""));
		mpassword.setText(prefs.getString(PREFPASS, ""));
		maddress.setText(prefs.getString(PREFADDR, ""));
		mport.setText(String.valueOf(prefs.getInt(PREFPORT, 0)));

		final Activity settings = this;
		msave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				save();
				settings.finish();
			}
		});	
		mcancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				settings.finish();
			}
		});
		
		setContentView(layout);
	}
	
	private void save() {
		SharedPreferences prefs = getSharedPreferences(PREFFILE, 0);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(PREFUSER, musername.getText().toString());
		edit.putString(PREFPASS, mpassword.getText().toString());
		edit.putString(PREFADDR, maddress.getText().toString());
		edit.putInt(PREFPORT, Integer.valueOf(mport.getText().toString()));
		edit.commit();
	}
}

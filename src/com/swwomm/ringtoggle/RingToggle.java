package com.swwomm.ringtoggle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class RingToggle extends Activity {
	
	protected static final String ABOUT_PAGE = "http://code.google.com/p/ringtoggle/wiki/About";
	protected static final String VERSION_FRAGMENT = "#Version_";
	
	protected BroadcastReceiver m_ringerModeReceiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // add ring-mode radio-group onchange listener
        RadioGroup group = (RadioGroup) findViewById(R.id.menu);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.ring: ring(); break;
					case R.id.vibrate: vibrate(); break;
					case R.id.silent: silent(); break;
				}
			}
        });
        
        // add finish button onclick listener
        Button button = (Button) findViewById(R.id.finish);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RingToggle.this.finish();
			}
        });
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		// update radio-group to reflect changed ringer-mode
		updateRadioGroup();
		
		// register receiver to check for ringer-mode updates while running
		if (m_ringerModeReceiver == null) {
			m_ringerModeReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
			        updateRadioGroup();
				}
			};
			registerReceiver(m_ringerModeReceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
		}
	}
    
    @Override
	protected void onPause() {
    	// suspend ringer-mode receiver
    	if (m_ringerModeReceiver != null) {
	    	this.unregisterReceiver(m_ringerModeReceiver);
	    	m_ringerModeReceiver = null;
    	}
    	
		super.onPause();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuItem about = menu.add(R.string.about);
        about.setIcon(R.drawable.about);
        about.setIntent(new Intent(Intent.ACTION_VIEW, getAboutPageUri()));
        
        return true;
    }
    
    protected Uri getAboutPageUri() {
    	try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	        return Uri.parse(ABOUT_PAGE + VERSION_FRAGMENT + version);
	        
		} catch (NameNotFoundException e) {
	        return Uri.parse(ABOUT_PAGE);
		} 
    }
    
    protected void updateRadioGroup() {
        int checkedId = current();
        RadioButton checked = (RadioButton) findViewById(checkedId);
        checked.setChecked(true);
    }

	protected int current() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		switch (audio.getRingerMode()) {
			case AudioManager.RINGER_MODE_SILENT: return R.id.silent;
			case AudioManager.RINGER_MODE_VIBRATE: return R.id.vibrate;
			default: return R.id.ring;
		}
    }
    
    protected void ring() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
    	
    }
    
    protected void vibrate() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
    	
    }
    
    protected void silent() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
    	
    }
}
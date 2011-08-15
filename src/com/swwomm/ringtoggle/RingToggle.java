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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class RingToggle extends Activity {
	
	protected static final String ABOUT_PAGE = "http://code.google.com/p/ringtoggle/wiki/About";
	protected static final String VERSION_FRAGMENT = "#Version_";
	
	protected boolean m_ignoreChange;
	protected BroadcastReceiver m_ringerModeReceiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // close when background touched
        View view = (View) findViewById(R.id.view);
        view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				RingToggle.this.finish();
				return false;
			}
        });
        
        // add ring-mode radio-group onchange listener
        RadioGroup group = (RadioGroup) findViewById(R.id.menu);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (m_ignoreChange)
					return;
				
				switch (checkedId) {
					case R.id.ring_and_vibrate: ringAndVibrate(); break;
					case R.id.ring: ring(); break;
					case R.id.vibrate: vibrate(); break;
					case R.id.silent: silent(); break;
				}
				
		        RadioButton radio = (RadioButton) findViewById(checkedId);
		        if (radio != null)
		        	radio.setTextSize(30);
				
				RingToggle.this.close();
			}
        });
        
        // add listener to each radio button to close app when clicked
        //(even when button is already selected)
        for (int i = 0, l = group.getChildCount(); i < l; i++) {
	        RadioButton radio = (RadioButton) group.getChildAt(i);
	        radio.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
			        RadioButton radio = (RadioButton) v;
					if (!radio.isChecked())
						return false;
					
		        	radio.setTextSize(30);
					RingToggle.this.close();
					
					return false;
				}
	        });
        }
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
        
        MenuItem help = menu.add(R.string.help);
        help.setIcon(R.drawable.help);
        help.setIntent(new Intent(this, Help.class));
        
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
        m_ignoreChange = true;
        checked.setChecked(true);
        m_ignoreChange = false;
    }

	protected int current() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		switch (audio.getRingerMode()) {
			case AudioManager.RINGER_MODE_SILENT: return R.id.silent;
			case AudioManager.RINGER_MODE_VIBRATE: return R.id.vibrate;
		}
		
		if (audio.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER))
			return R.id.ring_and_vibrate;
		
		return R.id.ring;
    }
    
    protected void ringAndVibrate() {
    	broadcastVolumeUpdate(AudioManager.RINGER_MODE_NORMAL);
    	
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
    }
    
    protected void ring() {
    	broadcastVolumeUpdate(AudioManager.RINGER_MODE_NORMAL);
    	
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
    }
    
    protected void vibrate() {
    	broadcastVolumeUpdate(AudioManager.RINGER_MODE_VIBRATE);
    	
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
    }
    
    protected void silent() {
    	broadcastVolumeUpdate(AudioManager.RINGER_MODE_SILENT);
    	
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
		audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
    }
    
    protected void close() {
    	new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				RingToggle.this.finish();
			}
    	}.start();
    }
    
    protected void broadcastVolumeUpdate(int ringerMode) {
        // see http://www.openintents.org/en/node/380
        Intent intent = new Intent("org.openintents.audio.action_volume_update");
        intent.putExtra("org.openintents.audio.extra_stream_type", AudioManager.STREAM_RING);
        intent.putExtra("org.openintents.audio.extra_volume_index", -9999);
        intent.putExtra("org.openintents.audio.extra_ringer_mode", ringerMode);
        getApplicationContext().sendOrderedBroadcast(intent, null);
    }
}
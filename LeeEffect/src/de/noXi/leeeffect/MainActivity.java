/*
 * This file is part of LeeEffect for Android.
 * 
 * LeeEffect for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.noXi.leeeffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * The Class MainActivity.
 */
public class MainActivity extends Activity {
	
	/** The audio bytes buffer. */
	private byte[] audioBytesBuffer = null;
    
    /** The Constant DB_NAME. */
    private static final String DB_NAME = "LeeEffectDB";
    
    /** The Constant BITRATE_ID. */
    private static final String BITRATE_ID = "Bitrate";
    
    /** The Constant ISRECORDING_ID. */
    private static final String ISRECORDING_ID = "isRecording";
    
    /** The Constant ENCODING. */
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    
    /** The start stop button. */
    private ToggleButton startStopButton = null;
    
    /** The delay text view. */
    private TextView delayTextView = null;
    
    /** The Constant BITRATE. */
    private static final int BITRATE = 11025;
    
    /** The audio bytes buffer size. */
    private int audioBytesBufferSize = AudioRecord.getMinBufferSize(BITRATE, AudioFormat.CHANNEL_IN_MONO, ENCODING);
    
    /** The audio bytes buffer size min. */
    private int audioBytesBufferSizeMin = audioBytesBufferSize;
    
    /** The audio delay progress. */
    private int audioDelayProgress = 0;
    
    /** The recording state. */
    private Boolean isRecording = false;
    
    /** The audio thread. */
    private Thread audioThread = null;
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    }

    /**
     * Start the audio thread.
     */
    void startThread(){
    	stopThread();
    	audioThread = null;
    	audioThread = new Thread(){ 
	     	public void run() {
	     	     isRecording = true;
	     	     android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	     	     AudioRecord arec = null;
	     	     AudioTrack atrack = null;
	     	     int buffersize_old = 0;
	     	     
	
	     	           while(isRecording) {
	     	        	   if(buffersize_old != audioBytesBufferSize){
							   buffersize_old = audioBytesBufferSize;
							   
							   if(arec != null && atrack != null){
								   arec.stop();
								   atrack.stop();
							   }
							   
							   audioBytesBuffer = new byte[buffersize_old];
					       	   arec = new AudioRecord(MediaRecorder.AudioSource.MIC, BITRATE, AudioFormat.CHANNEL_IN_MONO, ENCODING, buffersize_old);
				        	   atrack = new AudioTrack(AudioManager.STREAM_MUSIC, BITRATE, AudioFormat.CHANNEL_OUT_MONO, ENCODING, buffersize_old, AudioTrack.MODE_STREAM);
				        	   atrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
				        	   atrack.setPlaybackRate(BITRATE);
							   arec.startRecording();
							   atrack.play();
							   //widget.setStreamId(atrack.getAudioSessionId());
	     	        	   }
	     	               arec.read(audioBytesBuffer, 0, buffersize_old); //  buffersize/bitrate = seconds
	     	               atrack.write(audioBytesBuffer, 0, audioBytesBuffer.length);
	     	               /*for(i_r=0; i_r<buffer.length-2; i_r+=2){
	     	            	  if(buffer.length-i_r != 1){
	         	            	  mainHandler.post(myRunnable);
	     	            	  }
	     	               }*/
	     	           }
	     	           
	     	          arec.stop();
					  atrack.stop();
	     	     } 
         };
    	audioThread.start();
    	Toast.makeText(this, "Alive: " + (audioThread!=null && audioThread.isAlive()), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stop the audio thread (join).
     */
    void stopThread(){
    	isRecording = false;
    	if(audioThread!=null) {
	    	try {
				audioThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Update text for user.
     */
    void updateText()
    {
    	if(delayTextView != null)
    		audioBytesBufferSize = (audioDelayProgress+1) * audioBytesBufferSizeMin;
    		delayTextView.setText("Verzögerung: "+(audioBytesBufferSize*1000/BITRATE/2)+"ms");
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	startStopButton = (ToggleButton) findViewById(R.id.toggleButton1);
    	startStopButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					startThread();
				}else{
					stopThread();
				}
				
			}
		});
    	
    	
    	delayTextView = (TextView) this.findViewById(R.id.textView2);
    	SeekBar bar = (SeekBar) this.findViewById(R.id.seekBar1);
    	bar.setMax(9);
    	bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				audioDelayProgress = progress;
				updateText();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
    	
    	bar.setProgress(this.getSharedPreferences(DB_NAME, MODE_PRIVATE).getInt(BITRATE_ID, 1));
    	audioDelayProgress = bar.getProgress();
    	updateText();
        startStopButton.setChecked(this.getSharedPreferences(DB_NAME, MODE_PRIVATE).getBoolean(ISRECORDING_ID, false));
        if(startStopButton.isChecked()){
        	startThread();
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @SuppressLint("NewApi") @Override
    protected void onPause() {
    	super.onPause();
    	stopThread();
    	
    	this.getSharedPreferences(DB_NAME, MODE_PRIVATE)
    		.edit()
    		.putInt(BITRATE_ID, audioBytesBufferSize/audioBytesBufferSizeMin -1)
    		.putBoolean(ISRECORDING_ID, startStopButton!=null && startStopButton.isChecked())
    		.apply();
    	
    }

    /**
     * There is no menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }
    
}

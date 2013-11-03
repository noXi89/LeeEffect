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

public class MainActivity extends Activity {
	
	private byte[] audioBytesBuffer = null;
    private static final String DB_NAME = "LeeEffectDB";
    private static final String BITRATE_ID = "Bitrate";
    private static final String ISRECORDING_ID = "isRecording";
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private ToggleButton startStopButton = null;
    private TextView delayTextView = null;
    private static final int bitrate = 11025;
    private int audioBytesBufferSize = AudioRecord.getMinBufferSize(bitrate, AudioFormat.CHANNEL_IN_MONO, ENCODING);
    private int audioBytesBufferSizeMin = audioBytesBufferSize;
    private int audioDelayProgress = 0;
    private Boolean isRecording = false;
    private Thread audioThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    }

    void startT(){
    	stopT();
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
					       	   arec = new AudioRecord(MediaRecorder.AudioSource.MIC, bitrate, AudioFormat.CHANNEL_IN_MONO, ENCODING, buffersize_old);
				        	   atrack = new AudioTrack(AudioManager.STREAM_MUSIC, bitrate, AudioFormat.CHANNEL_OUT_MONO, ENCODING, buffersize_old, AudioTrack.MODE_STREAM);
				        	   atrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
				        	   atrack.setPlaybackRate(bitrate);
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
    
    void stopT(){
    	isRecording = false;
    	if(audioThread!=null) {
	    	try {
				audioThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    void updateText()
    {
    	if(delayTextView != null)
    		audioBytesBufferSize = (audioDelayProgress+1) * audioBytesBufferSizeMin;
    		delayTextView.setText("Verzögerung: "+(audioBytesBufferSize*1000/bitrate/2)+"ms");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	startStopButton = (ToggleButton) findViewById(R.id.toggleButton1);
    	startStopButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					startT();
				}else{
					stopT();
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
        	startT();
        }
    }

    @SuppressLint("NewApi") @Override
    protected void onPause() {
    	super.onPause();
    	stopT();
    	
    	this.getSharedPreferences(DB_NAME, MODE_PRIVATE)
    		.edit()
    		.putInt(BITRATE_ID, audioBytesBufferSize/audioBytesBufferSizeMin -1)
    		.putBoolean(ISRECORDING_ID, startStopButton!=null && startStopButton.isChecked())
    		.apply();
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

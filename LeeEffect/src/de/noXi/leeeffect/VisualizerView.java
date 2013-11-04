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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

/**
 * The Class VisualizerView. Visualizes audio from specified <a href="http://developer.android.com/reference/android/media/AudioRecord.html#getAudioSessionId()">AudioSessionID</a>.<br>
 * Uses <a href="http://developer.android.com/reference/android/media/AudioRecord.html#getAudioSessionId()">AudioSessionID</a> <b>0</b> (default speaker output) by default.<br>
 * 
 * @see <a href="http://developer.android.com/reference/android/media/AudioRecord.html#getAudioSessionId()">android.media.AudioRecord#getAudioSessionId()</a>
 */
public class VisualizerView extends View {

	/** The bytes. */
	private byte[] bytes;
	
	/** The points. */
	private float[] points;
	
	/** The rect. */
	private Rect rect = new Rect();
	
	/** The fore paint. */
	private Paint forePaint = new Paint();
	
	/** The back paint. */
	private Paint backPaint = new Paint();
	
	/** The visualizer. */
	private Visualizer visualizer;

	/**
	 * Instantiates a new visualizer view.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//prefs = PreferenceManager.getDefaultSharedPreferences(context);
		initDraw();
		initVisualizer();
	}

	/**
	 * Inits the visualizer.
	 */
	private void initVisualizer(){
		visualizer = new Visualizer(0);
		visualizer.setEnabled(false);

		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
		visualizer.setDataCaptureListener(
				new Visualizer.OnDataCaptureListener() {

					public void onWaveFormDataCapture(Visualizer visualizer,
							byte[] bytes, int samplingRate) {
						updateVisualizer(bytes);
					}

					public void onFftDataCapture(Visualizer visualizer,
							byte[] bytes, int samplingRate) {
					}
				}, Visualizer.getMaxCaptureRate(), true, true);
		visualizer.setEnabled(true);
	}

	/**
	 * Inits the paint for draw.
	 */
	private void initDraw() {
		bytes = null;
		//int colorchosen = prefs.getInt("COLOR_PREFERENCE_KEY",
		//      Color.WHITE);
		forePaint.setStrokeWidth(1);
		//mForePaint.setAntiAlias(true);
		forePaint.setColor(Color.WHITE);
		forePaint.setAntiAlias(true);
		forePaint.setStrokeWidth(3);
		//mForePaint.setMaskFilter(new BlurMaskFilter(1, Blur.INNER));
		backPaint.setColor(Color.BLACK);
	}

	/**
	 * Update visualizer with new <code>bytes</code>, forces redraw.
	 *
	 * @param bytes the bytes
	 */
	public void updateVisualizer(byte[] bytes) {
		this.bytes = bytes;
		invalidate();
	}

	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (bytes == null) {
			return;
		}
		if (points == null || points.length < bytes.length * 4) {
			points = new float[bytes.length * 4];
		}

		rect.set(0, 0, getWidth(), getHeight());
		canvas.drawRect(rect, backPaint);
		for (int i = 0; i < bytes.length - 1; i++) {
			points[i * 4] = rect.width() * i / (bytes.length - 1);
			points[i * 4 + 1] = rect.height() / 2
					+ ((byte) (bytes[i] + 128)) * (rect.height() / 2) / 128;
			points[i * 4 + 2] = rect.width() * (i + 1) / (bytes.length - 1);
			points[i * 4 + 3] = rect.height() / 2
					+ ((byte) (bytes[i + 1] + 128)) * (rect.height() / 2)
					/ 128;
		}

		canvas.drawLines(points, forePaint);
		//canvas.drawPoints(mPoints, mForePaint);
	}
}
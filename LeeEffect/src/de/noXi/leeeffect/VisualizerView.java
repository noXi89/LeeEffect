package de.noXi.leeeffect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

	private byte[] bytes;
	private float[] points;
	private Rect rect = new Rect();
	private Paint forePaint = new Paint();
	private Paint backPaint = new Paint();
	private Visualizer visualizer;

	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//prefs = PreferenceManager.getDefaultSharedPreferences(context);
		init();
		initVisualizer();
	}

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

	private void init() {
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

	public void updateVisualizer(byte[] bytes) {
		this.bytes = bytes;
		invalidate();
	}

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
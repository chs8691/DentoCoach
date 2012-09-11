package de.cs.android.putzi;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class PutziService extends Service {
	private static final String TAG = "PutziService";
	private long leavingMs = 0;
	CountDownTimer timer = null;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v(TAG, "onBind()");
		return null;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate()");
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		if (timer != null) {
			timer.cancel();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");

		leavingMs = intent.getLongExtra("durationMs", 10000);
		timer = new CountDownTimer(leavingMs, 1000) {

			@Override
			public void onFinish() {
				stopSelf();

			}

			@Override
			public void onTick(long millisUntilFinished) {
				Log.v(TAG, "onTick(): " + millisUntilFinished);

			}
		};
		timer.start();

		// We don't want this service to continue running after canceled by the
		// system.
		return START_NOT_STICKY;
	}

}

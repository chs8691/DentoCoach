package de.cs.android.putzi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class TimerService extends Service {
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		// /**
		// * Access and initialize the service.
		// *
		// * @param A
		// * TickListener to update view etc. every tick
		// * @param durationSec
		// * @return
		// */
		// TimerService getService(TickListener tickListener, int durationSec) {
		// TimerService.this.tickListener = tickListener;
		// TimerService.this.durationSec = durationSec;
		// return TimerService.this;
		// }

		TimerService getService() {
			Log.v(TAG, "getService()");
			return TimerService.this;
		}

		// /**
		// * Stopps the timer without a reset. Continue with start()
		// */
		// void pause() {
		//
		// }
		//
		// /**
		// * Start or restarts the timer
		// */
		// void start() {
		//
		// }

	}

	/**
	 * Listenr on timer events
	 * 
	 * @author ChristianSchulzendor
	 * 
	 */
	interface TickListener {

		/**
		 * Called when timer is started
		 */
		void onStart(long leavingMs);

		/**
		 * Called before timer will be stopped
		 * 
		 * @param long with leaving time in ms
		 */
		void onStop(long leavingMs);

		/**
		 * Called with every timer tick
		 * 
		 * @param long with leaving time in ms
		 */
		void onTick(long leavingMs);
	}

	private CountDownTimer timer = null;
	private TickListener tickListener = null;

	// /**
	// * Will be executed with every tick of the timer, e.g. every second
	// *
	// * @author ChristianSchulzendor
	// *
	// */
	// public interface TickListener {
	// void onTick();
	// }

	// private TickListener tickListener;
	//
	// private int durationSec;

	private static final String TAG = "TimerService";

	private NotificationManager nMgr;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private final int NOTIFICATION = 1;

	private final IBinder binder = new LocalBinder();

	/**
	 * Leaving time of the timer in ms
	 */
	private long leavingMs;

	/**
	 * Time in ms of next tick to raise
	 */
	private long nextMs;

	private static final long INTERVALL_MS = 10;

	public long getLeavingMs() {
		return leavingMs;
	}

	public boolean isRunning() {

		return timer != null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate()");

		// Only instantiate Notification Manager
		nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	@Override
	public void onDestroy() {

		Log.v(TAG, "onDestroy()");
		if (timer != null)

			nMgr.cancel(NOTIFICATION);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");

		// We don't want this service to continue running after canceled by the
		// system.
		return START_NOT_STICKY;
	}

	public void setLeavingMs(long leavingMs) {
		this.leavingMs = leavingMs;
	}

	public void setTickListener(TickListener tickListener) {
		this.tickListener = tickListener;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.notificationStarted);

		// Set the icon, scrolling text and time stamp
		Notification notification = new Notification(R.drawable.notification,
				"", System.currentTimeMillis());

		// Notification must not be cleared by the user
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;

		Intent toLaunch = new Intent(getApplicationContext(),
				MainActivity.class);
		// Two magic coding lines: Return to the running Activity
		toLaunch.setAction("android.intent.action.MAIN");
		toLaunch.addCategory("android.intent.category.LAUNCHER");
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, toLaunch, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.noticicationTitle), "", contentIntent);

		// Send the notification.
		nMgr.notify(NOTIFICATION, notification);
	}

	public void start() {
		Log.v(TAG, "start() with leavingMs=" + leavingMs);
		nextMs = leavingMs;
		timer = new CountDownTimer(leavingMs, INTERVALL_MS) {

			/**
			 * End of timer: Stop service
			 */
			@Override
			public void onFinish() {
				Log.v(TAG, "onFinish()");
				leavingMs = nextMs = 0;
				nMgr.cancel(NOTIFICATION);
				stop();
			}

			@Override
			public void onTick(long pLeavingMs) {
				leavingMs = pLeavingMs;
				if (leavingMs <= nextMs) {
					Log.v(TAG, "Timer.onTick() with leavingMs=" + leavingMs);
					if (tickListener != null) {
						tickListener.onTick(leavingMs);
					}
					// refreshView(millisUntilFinished);
					nextMs -= 1000;
				}

			}

		};
		timer.start();
		tickListener.onStart(leavingMs);
		showNotification();
	}

	public void stop() {
		Log.v(TAG, "stop()");
		if (timer != null) {
			timer.cancel();
			timer = null;

		}
		if (tickListener != null) {
			tickListener.onStop(leavingMs);
		}
		nMgr.cancel(NOTIFICATION);
		stopSelf();

	}

}
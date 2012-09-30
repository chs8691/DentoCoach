package de.cs.android.putzi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class TimerService extends Service {
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {

		TimerService getService() {
			Log.v(TAG, "getService()");
			return TimerService.this;
		}

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

	private WakeLock wakeLock = null;

	private CountDownTimer timer = null;
	private TickListener tickListener = null;

	private static final String TAG = "TimerService";

	private NotificationManager nMgr;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private final int NOTIFICATION_RUNNING = 1;
	private final int NOTIFICATION_FINISHED = 2;

	private final IBinder binder = new LocalBinder();

	/**
	 * Leaving time of the timer in ms
	 */
	private long leavingMs;

	private Uri soundUri = null;

	/**
	 * Time in ms of next tick to raise
	 */
	private long nextMs;

	private static final long INTERVALL_MS = 10;

	// private WakeLock wakeLock;

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

			nMgr.cancel(NOTIFICATION_RUNNING);
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

	public void setSound(Uri soundUri) {
		this.soundUri = soundUri;
	}

	public void setTickListener(TickListener tickListener) {
		this.tickListener = tickListener;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotificationFinished() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.notificationFinished);

		// Set the icon, scrolling text and time stamp
		Notification notification = new Notification(
				R.drawable.notification_finished,
				getText(R.string.notificationFinished),
				System.currentTimeMillis());

		// Notification must not be cleared by the user
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

		// Play sound at the end, if it has been configured
		if (soundUri != null)
			notification.sound = soundUri;

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
				getText(R.string.noticicationTitle),
				getText(R.string.notificationFinished), contentIntent);

		// Send the notification.
		nMgr.notify(NOTIFICATION_FINISHED, notification);
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotificationRunning() {
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
		nMgr.notify(NOTIFICATION_RUNNING, notification);
	}

	public void start() {
		Log.v(TAG, "start() with leavingMs=" + leavingMs);
		nextMs = leavingMs;

		// Remove previous notifications if exists
		nMgr.cancel(NOTIFICATION_FINISHED);
		nMgr.cancel(NOTIFICATION_RUNNING);

		timer = new CountDownTimer(leavingMs, INTERVALL_MS) {

			/**
			 * End of timer: Stop service
			 */
			@Override
			public void onFinish() {
				Log.v(TAG, "onFinish()");
				leavingMs = nextMs = 0;
				showNotificationFinished();
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
		showNotificationRunning();

		// Switch on wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, TAG);
		// wakeLock without time: what happens if Service get killed by the
		// Android system?
		wakeLock.acquire();
		Log.v(TAG, "Acquire wackLock");

	}

	public void stop() {
		Log.v(TAG, "stop()");
		if (timer != null) {
			timer.cancel();
			timer = null;

			// Switch off wake lock
			if (wakeLock != null) {
				Log.v(TAG, "Release WakeLock ...");
				wakeLock.release();
			}

		}
		if (tickListener != null) {
			tickListener.onStop(leavingMs);
		}
		nMgr.cancel(NOTIFICATION_RUNNING);

		stopSelf();

	}

}
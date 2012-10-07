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

//**************** TASKLIST ****************************
//TODO Notification color on all API-Levels (for WVA800)
//  7 ok
//  8 ok
// 10
// 15 ok
// 16 ok

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
		 * Called with every new brush position (step)
		 * 
		 * @param int actual step number. Will be increased with every call
		 */
		void onStepChange(int stepNr);

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
	 * Total duration in MS
	 */
	private long durationMs;
	/**
	 * Actual leaving time of the timer in ms
	 */
	private long actLeavingMs;

	/**
	 * Time in MS o the next step
	 */
	private long nextStepMs;

	private Uri soundUri = null;

	/**
	 * Time in ms of next tick to raise
	 */
	private long nextMs;

	private static final long INTERVALL_MS = 10;

	private int stepDurationMs;

	// private WakeLock wakeLock;

	/**
	 * Calculates actual step number
	 * 
	 * @return stepNr int
	 */
	public int getActStepNr() {
		return (int) ((durationMs - actLeavingMs) / stepDurationMs);
	}

	public long getLeavingMs() {
		return actLeavingMs;
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

	/**
	 * Resets the timer to particular time point, e.g. to restart a stopped
	 * timer. Animation value will be determined.
	 */
	public void rebuildPreviousState(long leavingMs) {
		actLeavingMs = leavingMs;
	}

	/**
	 * Resets the timer to initial state, e.g. after reset Button pressed. All
	 * values will set to initial
	 */
	public void resetTimer() {
		actLeavingMs = durationMs;
	}

	/**
	 * Initializes the timer with the default setting
	 * 
	 * @param durationMs
	 *            long with time until finish
	 * @param stepDurationMs
	 *            int for how long one brushing step goes
	 * @param parts
	 *            constant int for how many parts are in one round
	 */
	public void setDefaultSettings(long durationMs, int stepDurationMs) {
		this.durationMs = durationMs;
		this.stepDurationMs = stepDurationMs;
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
		Log.v(TAG, "start() with leavingMs=" + actLeavingMs);
		nextMs = actLeavingMs;
		nextStepMs = actLeavingMs;

		// Remove previous notifications if exists
		nMgr.cancel(NOTIFICATION_FINISHED);
		nMgr.cancel(NOTIFICATION_RUNNING);

		timer = new CountDownTimer(actLeavingMs, INTERVALL_MS) {

			/**
			 * End of timer: Stop service
			 */
			@Override
			public void onFinish() {
				Log.v(TAG, "onFinish()");
				actLeavingMs = nextMs = 0;
				showNotificationFinished();
				stop();
			}

			@Override
			public void onTick(long pLeavingMs) {
				actLeavingMs = pLeavingMs;
				if (actLeavingMs <= nextMs) {
					Log.v(TAG, "Timer.onTick() with leavingMs=" + actLeavingMs);
					if (tickListener != null) {
						tickListener.onTick(actLeavingMs);
					}
					nextMs -= 1000;
				}
				if (actLeavingMs <= nextStepMs) {
					Log.v(TAG, "Timer.onTick() reached next step");
					if (tickListener != null) {
						// Starts with step number 0, increase AFTER calling
						tickListener.onStepChange(getActStepNr());
					}
					nextStepMs -= stepDurationMs;
				}

			}

		};

		timer.start();

		tickListener.onStart(actLeavingMs);
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
			tickListener.onStop(actLeavingMs);
		}
		nMgr.cancel(NOTIFICATION_RUNNING);

		stopSelf();

	}

}
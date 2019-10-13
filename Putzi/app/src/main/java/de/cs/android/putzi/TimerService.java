package de.cs.android.putzi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static de.cs.android.putzi.MainActivity.CHANNEL_ID_FINISHED;
import static de.cs.android.putzi.MainActivity.CHANNEL_ID_STARTED;


public class TimerService extends Service {
    private static final String TAG = TimerService.class.getSimpleName();
    private static final long INTERVALL_MS = 10;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private final int NOTIFICATION_RUNNING = 1;
    private final int NOTIFICATION_FINISHED = 2;
    private final IBinder binder = new LocalBinder();
    private WakeLock wakeLock = null;
    private CountDownTimer timer = null;
    private TickListener tickListener = null;
    private NotificationManager notificationManager;
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
    private int stepDurationMs;

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

    // private WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");

        // Only instantiate Notification Manager
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


    }

    @Override
    public void onDestroy() {

        Log.v(TAG, "onDestroy()");
        if (timer != null)

            notificationManager.cancel(NOTIFICATION_RUNNING);
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
     * @param durationMs     long with time until finish
     * @param stepDurationMs int for how long one brushing step goes
     */
    public void setDefaultSettings(long durationMs, int stepDurationMs) {
        this.durationMs = durationMs;
        this.stepDurationMs = stepDurationMs;
    }

    public void setSound(Uri soundUri) {
        this.soundUri = soundUri;
        Log.d(TAG, String.format("Set sound=$1%s", soundUri.getPath()));

    }

    public void setTickListener(TickListener tickListener) {
        this.tickListener = tickListener;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotificationFinished() {


        Intent toLaunch = new Intent(getApplicationContext(),
                MainActivity.class);
        // Two magic coding lines: Return to the running Activity
        toLaunch.setAction("android.intent.action.MAIN");
        toLaunch.addCategory("android.intent.category.LAUNCHER");
        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, toLaunch, 0);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Set the info for the views that show in the notification panel.
            notification = new NotificationCompat.Builder(this,
                    CHANNEL_ID_FINISHED)
                    .setContentTitle(getText(R.string.noticicationTitle))
                    .setContentTitle(getText(R.string.notificationFinished))
                    .setSmallIcon(R.drawable.notification_finished)
                    .setAutoCancel(true)
                    .addAction(R.drawable.notification, getText(R.string.noticicationTitle),
                            contentIntent)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getText(R.string.noticicationTitle))
                    .setContentTitle(getText(R.string.notificationFinished))
                    .setSmallIcon(R.drawable.notification_finished)
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .addAction(R.drawable.notification, getText(R.string.noticicationTitle),
                            contentIntent)
                    .build();

        }
        // Send the notification.
        notificationManager.notify(NOTIFICATION_FINISHED, notification);
    }


    /**
     * Show a notification while this service is running.
     */
    private void showNotificationStarted() {


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
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_STARTED)
                .setContentTitle(getText(R.string.noticicationTitle))

                // Notification must not be cleared by the user
                .setOngoing(true)

                .setSmallIcon(R.drawable.notification)

                .addAction(R.drawable.notification, getText(R.string.noticicationTitle),
                        contentIntent)
                .build();

        // Send the notification.
        notificationManager.notify(NOTIFICATION_RUNNING, notification);
    }

    public void start() {
        Log.v(TAG, "start() with leavingMs=" + actLeavingMs);
        nextMs = actLeavingMs;
        nextStepMs = actLeavingMs;

        // Remove previous notifications if exists
        notificationManager.cancel(NOTIFICATION_FINISHED);
        notificationManager.cancel(NOTIFICATION_RUNNING);

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
        showNotificationStarted();

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
        notificationManager.cancel(NOTIFICATION_RUNNING);

        stopSelf();

    }

    /**
     * Listenr on timer events
     *
     * @author ChristianSchulzendor
     */
    interface TickListener {

        /**
         * Called when timer is started
         */
        void onStart(long leavingMs);

        /**
         * Called with every new brush position (step)
         *
         * @param stepNr actual step number. Will be increased with every call
         */
        void onStepChange(int stepNr);

        /**
         * Called before timer will be stopped
         *
         * @param leavingMs with leaving time in ms
         */
        void onStop(long leavingMs);

        /**
         * Called with every timer tick
         *
         * @param leavingMs with leaving time in ms
         */
        void onTick(long leavingMs);
    }

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

}
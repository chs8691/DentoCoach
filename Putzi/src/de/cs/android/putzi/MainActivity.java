package de.cs.android.putzi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	private class AnimationController implements AnimationListener {
		private static final String TAG = "AnimationController";

		private Speed speed;
		private static final String AC_RUN = "acRun";
		private static final String AC_POS = "acPos";
		private static final String AC_ROUND = "acRound";
		private RoundListener roundListener = null;
		private int pos;
		private boolean run = true;
		private int round = 0;
		private Part actPart = null;

		private final List<Part> partList = new ArrayList<Part>();

		public AnimationController(Speed speed) {

			setSpeed(speed);
			builder();
			reset(null);
		}

		private void builder() {
			// upper out
			partList.add(new Part(R.id.brush_ulo, getPartDuration(), 20, -40,
					this));
			partList.add(new Part(R.id.brush_ufo, getPartDuration(), 40, 0,
					this));
			partList.add(new Part(R.id.brush_uro, getPartDuration(), 20, 40,
					this));

			// lower out
			partList.add(new Part(R.id.brush_lro, getPartDuration(), -20, 40,
					this));
			partList.add(new Part(R.id.brush_lfo, getPartDuration(), -40, 0,
					this));
			partList.add(new Part(R.id.brush_llo, getPartDuration(), -20, -40,
					this));

			// lower in
			partList.add(new Part(R.id.brush_lli, getPartDuration(), 20, 40,
					this));
			partList.add(new Part(R.id.brush_lfi, getPartDuration(), 40, 0,
					this));
			partList.add(new Part(R.id.brush_lri, getPartDuration(), 20, -40,
					this));

			// upper in
			partList.add(new Part(R.id.brush_uri, getPartDuration(), -20, -40,
					this));
			partList.add(new Part(R.id.brush_ufi, getPartDuration(), -40, 0,
					this));
			partList.add(new Part(R.id.brush_uli, getPartDuration(), -20, 40,
					this));

			// upper top
			partList.add(new Part(R.id.brush_ult, getPartDuration(), 20, -40,
					this));
			partList.add(new Part(R.id.brush_uft, getPartDuration(), 40, 0,
					this));
			partList.add(new Part(R.id.brush_urt, getPartDuration(), 20, 40,
					this));

			// lower top
			partList.add(new Part(R.id.brush_lrt, getPartDuration(), -20, 40,
					this));
			partList.add(new Part(R.id.brush_lft, getPartDuration(), -40, 0,
					this));
			partList.add(new Part(R.id.brush_llt, getPartDuration(), -20, -40,
					this));

		}

		private int getPartDuration() {
			return (speed.value() * 1000);
		}

		private void next() {
			if (!run)
				return;

			if (pos >= partList.size()) {
				pos = 0;
				roundListener.onRoundChanged(++round);
			}

			if (actPart != null)
				actPart.setInvisible();

			Log.v(TAG, "AnimationController.next(): pos=" + pos);
			actPart = partList.get(pos++);
			actPart.start();

		}

		public void onAnimationEnd(Animation animation) {
			next();

		}

		public void onAnimationRepeat(Animation animation) {
			// Nothing to do
		}

		public void onAnimationStart(Animation animation) {
			// Nothing to do

		}

		/**
		 * 
		 * @param saveInstanceState
		 *            Bundle with the state to restore or null
		 */
		public void reset(Bundle saveInstanceState) {
			if (saveInstanceState != null) {
				this.pos = saveInstanceState.getInt(AC_POS);
				this.round = saveInstanceState.getInt(AC_ROUND);
				Log.v(TAG, "AnimationController.reset(): Bundle pos=" + pos);
			} else {
				this.pos = 0;
				this.round = 0;
				Log.v(TAG, "AnimationController.reset(): no Bundle pos=" + pos);
			}

			// hide all old brush positions, not fine but works
			for (Part aPart : partList) {
				aPart.setInvisible();
			}

			// Show start brush
			actPart = partList.get(pos);
			actPart.showImage();

			this.run = false;

		}

		void setRoundListener(RoundListener roundListener) {
			this.roundListener = roundListener;
		}

		public void setSpeed(Speed speed) {
			this.speed = speed;
		}

		private void start() {
			if (!run)
				run = true;
			next();
		}

		private void stop() {
			if (run) {
				run = false;
				pos--;
			}
			Log.v(TAG, "AnimationController.stop():  run/pos:" + run + "/"
					+ pos);
		}

		private void storeState(Bundle outState) {
			Log.v(TAG, "stroreState(): pos/round/run=" + pos + "/" + round
					+ "/" + run);
			outState.putInt(AC_POS, this.pos);
			outState.putInt(AC_ROUND, this.round);
			outState.putBoolean(AC_RUN, this.run);
		}

	}

	enum BundlePreviousState {
		NULL, RUNNING, STOPPED
	}

	class Part {
		private final int duration;
		private final float x;
		private final float y;
		private final AnimationListener listener;
		private final ImageView i;

		private Part(int id, int duration, float x, float y,
				AnimationListener listener) {
			this.duration = duration;
			this.x = x;
			this.y = y;
			this.listener = listener;
			this.i = (ImageView) findViewById(id);

		}

		void setInvisible() {
			i.clearAnimation();
			i.setVisibility(ImageView.INVISIBLE);
		}

		void showImage() {

			i.setVisibility(ImageView.VISIBLE);
		}

		void start() {
			showImage();
			AnimationSet set = new AnimationSet(false);

			Animation aniMove = new TranslateAnimation(0, x, 0, y);
			aniMove.setDuration(duration);
			aniMove.setAnimationListener(listener);
			set.addAnimation(aniMove);

			i.startAnimation(set);

		}
	}

	private static final String BUNDLE_PREVIOUS_STATE_KEY = "PreviousStateKey";

	private static final String BUNDLE_LEAVING_MS = "leavingMs";

	private final Date date = new Date(0);

	private ProgressBar progressBar;

	private static final String DURATION_FORMAT = "%1$tM:%1$tS";

	private static final String TAG = "PutziActivity";

	private Button startBtn = null;
	private Button resetBtn = null;
	private TextView roundView = null;
	private static final int SHOW_PREFERENCES = 1;

	private AnimationController animationController = null;

	private SettingValues settings;

	// private static final int DIALOG_ABOUT_ID = 1;

	private TimerService timerService;

	private boolean timerServiceIsBound;
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		private static final String TAG = "serviceConnection";

		public void onServiceConnected(ComponentName name, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a
			// explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.v(TAG, "onServiceConnected()");
			timerService = ((TimerService.LocalBinder) service).getService();
			Log.v(TAG, "timerService=" + timerService);

			// Initialize activities timer dependent objects

			// Timer still running
			if (timerService.isRunning()) {
				Log.v(TAG, "Timer is running");
				startBtn.setText(R.string.startButtonStop);
				animationController.start();
				resetBtn.setEnabled(false);
			}

			// No running timer
			else {
				Log.v(TAG, "Timer is not running");

				// Timer was running at last activity stop but meanwhile has
				// finished
				if (bundlePreviousState.equals(BundlePreviousState.RUNNING)) {
					Log.v(TAG, "Previous state=running");
					timerService.setLeavingMs(settings.getDurationMs());

				}
				// Last Session closed with a stopped timer
				else if (bundlePreviousState
						.equals(BundlePreviousState.STOPPED)) {
					Log.v(TAG, "Previous state=stopped");
				}
				// New Session
				else {
					Log.v(TAG, "No previous state");
					timerService.setLeavingMs(bundledLeavingMs);
				}

				startBtn.setText(R.string.startButtonStart);
				animationController.stop();
				resetBtn.setEnabled(true);
			}

			refreshTimerView();

			Log.v(TAG, "LeavingMS=" + timerService.getLeavingMs());

			timerService.setTickListener(new TimerService.TickListener() {

				public void onStart(long pLeavingMs) {
					startBtn.setText(R.string.startButtonStop);
					refreshTimerView();
					animationController.start();
					resetBtn.setEnabled(false);
				}

				public void onStop(long pLeavingMs) {
					startBtn.setText(R.string.startButtonStart);
					refreshTimerView();
					animationController.stop();
					resetBtn.setEnabled(true);
				}

				public void onTick(long pLeavingMs) {
					refreshTimerView();
				}
			});

		}

		public void onServiceDisconnected(ComponentName name) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			timerService = null;
		}

	};

	private TextView timerView = null;

	private long bundledLeavingMs;
	private BundlePreviousState bundlePreviousState = BundlePreviousState.NULL;

	/**
	 * Establish a connection with the service if needed. We use an explicit
	 * class name because we want a specific service implementation that we know
	 * will be running in our own process (and thus won't be supporting
	 * component replacement by other applications).
	 */
	private void doBindService() {
		if (!timerServiceIsBound) {
			timerServiceIsBound = bindService(new Intent(this,
					TimerService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
			Log.v(TAG, "doBindService(): new binding result="
					+ timerServiceIsBound);
		} else {
			Log.v(TAG, "doBindService(): binding still exists");
		}

	}

	private void doUnbindService() {
		if (timerServiceIsBound) {
			timerService.setTickListener(null);
			unbindService(serviceConnection);
			timerServiceIsBound = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SHOW_PREFERENCES) {
			// TODO was muss man nach Änderungen an den Einstellungen machen?
		}
	}

	/**
	 * If brushing, only hide app otherwise close
	 */
	@Override
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed()");

		if (timerService.isRunning())
			super.onBackPressed();
		else {
			this.moveTaskToBack(false);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.v(TAG, "onConfigurationChanged()");
		super.onConfigurationChanged(newConfig);
		// If enabled, switching to landscape doesn't change the layout.xml
		// if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
		// || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
		// }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// For very first call: Initialize preferences. Otherwise: do nothing
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		roundView = (TextView) findViewById(R.id.round);

		settings = new Settings(this);

		if (savedInstanceState != null) {
			if (savedInstanceState.getString(BUNDLE_PREVIOUS_STATE_KEY).equals(
					BundlePreviousState.RUNNING.name())) {
				bundlePreviousState = BundlePreviousState.RUNNING;
			} else {
				Log.v(TAG, "onCreate():set bundledLeavingMs="
						+ bundledLeavingMs);
				bundledLeavingMs = savedInstanceState
						.getLong(BUNDLE_LEAVING_MS);
				bundlePreviousState = BundlePreviousState.STOPPED;
			}
			bundlePreviousState = BundlePreviousState.NULL;
		}

		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		progressBar.setMax((int) settings.getDurationMs());

		timerView = (TextView) findViewById(R.id.count_down);

		// Update duration setting in view
		Date endTime = new Date(settings.getDurationMs());
		((TextView) findViewById(R.id.end_time)).setText(String.format(
				DURATION_FORMAT, endTime));

		animationController = new AnimationController(settings.getSpeed());
		animationController.reset(savedInstanceState);
		animationController.setRoundListener(new RoundListener() {

			public void onRoundChanged(int round) {
				roundView.setText(String.valueOf(round));
			}

		});

		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// Stop it
				if (timerService.isRunning()) {
					timerService.stop();
				}
				// Start it
				else {
					// in addition the service must have its own lifecycle
					startService(new Intent(getApplicationContext(),
							TimerService.class));

					timerService.setLeavingMs(settings.getDurationMs());
					timerService.start();

				}

			}

		});

		resetBtn = (Button) findViewById(R.id.resetBtn);
		resetBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.v(TAG, "resetBtn.onClick()");

				timerService.setLeavingMs(settings.getDurationMs());
				refreshTimerView();
				animationController.reset(null);
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivityForResult(new Intent(this, Pref.class),
					SHOW_PREFERENCES);

			return true;

		}
		return false;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.v(TAG, "onRestoreInstanceState()");
		super.onRestoreInstanceState(savedInstanceState);
		animationController.reset(savedInstanceState);
	}

	/** Called e.g. with orientation switch */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState()");

		if (animationController != null) {
			animationController.storeState(outState);
		}
		if (timerService.isRunning()) {
			Log.v(TAG, "onSaveInstanceState() Timer.isRunning=true");
			outState.putString(BUNDLE_PREVIOUS_STATE_KEY,
					BundlePreviousState.RUNNING.name());

		} else {
			Log.v(TAG,
					"onSaveInstanceState() save from stopped Timer LeavingMs="
							+ timerService.getLeavingMs());
			outState.putString(BUNDLE_PREVIOUS_STATE_KEY,
					BundlePreviousState.STOPPED.name());
			outState.putLong(BUNDLE_LEAVING_MS, timerService.getLeavingMs());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		Log.v(TAG, "onStart()");
		super.onStart();
		// bind and start the service (maybe it's still running but unbound
		doBindService();
	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop()");
		doUnbindService();
		super.onStop();

	}

	private void refreshTimerView() {
		progressBar.setProgress((int) (settings.getDurationMs() - timerService
				.getLeavingMs()));
		date.setTime(timerService.getLeavingMs());
		timerView.setText(String.format(DURATION_FORMAT, date));
	}

}

interface RoundListener {
	public void onRoundChanged(int round);
}

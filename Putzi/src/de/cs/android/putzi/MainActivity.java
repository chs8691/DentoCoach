package de.cs.android.putzi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
		private final RoundListener roundListener;
		private int pos;
		private boolean run = true;
		private int round = 0;
		private Part actPart = null;
		private ImageView image = null;

		private final List<Part> partList = new ArrayList<Part>();

		public AnimationController(Speed speed) {

			this.image = (ImageView) findViewById(R.id.image_brush);
			setSpeed(speed);
			builder();
			this.roundListener = new RoundListener() {

				public void onRoundChanged(int round) {
					roundView.setText(String.valueOf(round));
				}
			};

		}

		private void builder() {

			// upper out
			partList.add(new Part(R.drawable.brush_ulo, getPartDuration(), 20,
					-40, this, image));
			partList.add(new Part(R.drawable.brush_ufo, getPartDuration(), 40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_uro, getPartDuration(), 20,
					40, this, image));

			// lower out
			partList.add(new Part(R.drawable.brush_lro, getPartDuration(), -20,
					40, this, image));
			partList.add(new Part(R.drawable.brush_lfo, getPartDuration(), -40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_llo, getPartDuration(), -20,
					-40, this, image));

			// lower in
			partList.add(new Part(R.drawable.brush_lli, getPartDuration(), 20,
					40, this, image));
			partList.add(new Part(R.drawable.brush_lfi, getPartDuration(), 40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_lri, getPartDuration(), 20,
					-40, this, image));

			// upper in
			partList.add(new Part(R.drawable.brush_uri, getPartDuration(), -20,
					-40, this, image));
			partList.add(new Part(R.drawable.brush_ufi, getPartDuration(), -40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_uli, getPartDuration(), -20,
					40, this, image));

			// upper top
			partList.add(new Part(R.drawable.brush_ult, getPartDuration(), 20,
					-40, this, image));
			partList.add(new Part(R.drawable.brush_uft, getPartDuration(), 40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_urt, getPartDuration(), 20,
					40, this, image));

			// lower top
			partList.add(new Part(R.drawable.brush_lrt, getPartDuration(), -20,
					40, this, image));
			partList.add(new Part(R.drawable.brush_lft, getPartDuration(), -40,
					0, this, image));
			partList.add(new Part(R.drawable.brush_llt, getPartDuration(), -20,
					-40, this, image));

		}

		private int getPartDuration() {
			return (speed.value() * 1000);
		}

		void logSharedPreferences() {
			SharedPreferences sp = getPreferences(0);

			if (!sp.contains(AC_POS)) {
				Log.v(TAG, "animationController.logSharedPrefefences(): "
						+ AC_POS + " not found");
			} else {
				Log.v(TAG, "animationController.logSharedPreferences(): "
						+ AC_POS + "=" + sp.getInt(AC_POS, -1));

			}
			if (!sp.contains(AC_ROUND)) {
				Log.v(TAG, "animationController.logSharedPrefefences(): "
						+ AC_ROUND + " not found");
			} else {
				Log.v(TAG, "animationController.logSharedPreferences(): "
						+ AC_ROUND + "=" + sp.getInt(AC_ROUND, -1));

			}
			if (!sp.contains(AC_RUN)) {
				Log.v(TAG, "animationController.logSharedPrefefences(): "
						+ AC_RUN + " not found");
			} else {
				Log.v(TAG, "animationController.logSharedPreferences(): "
						+ AC_RUN + "=" + sp.getBoolean(AC_RUN, false));

			}

		}

		private void next() {
			if (!run)
				return;

			// Last one just shown
			if (pos >= partList.size()) {
				pos = 0;
				roundListener.onRoundChanged(++round);
			}
			// Not the last one: hide previous one
			else {
				if (actPart != null)
					actPart.setInvisible();
			}

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
			actPart.showImage();

		}

		public void reset(SharedPreferences pref) {
			if (pref != null) {
				this.pos = pref.getInt(AC_POS, 0);
				this.round = pref.getInt(AC_ROUND, 0);
				Log.v(TAG, "AnimationController.reset(): Last session pos="
						+ pos);
			} else {
				this.pos = 0;
				this.round = 0;
				Log.v(TAG, "AnimationController.reset(): no last session pos="
						+ pos);
			}

			// Show start brush
			actPart = partList.get(pos);
			actPart.showImage();

			// Set round view
			if (this.roundListener != null)
				this.roundListener.onRoundChanged(this.round);

			this.run = false;

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

		private void storeState(SharedPreferences.Editor editor) {
			Log.v(TAG, "stroreState(): pos/round/run=" + pos + "/" + round
					+ "/" + run);
			editor.putInt(AC_POS, this.pos);
			editor.putInt(AC_ROUND, this.round);
			editor.putBoolean(AC_RUN, this.run);
		}

	}

	enum InstanceState {
		NULL, RUNNING, STOPPED
	}

	private enum InstanceStateKey {

		PREVIOUS_STATE_KEY, LEAVING_MS;
	}

	class Part {
		private final int duration;
		private final float x;
		private final float y;
		private final AnimationListener listener;
		private final int drawableId;
		private final ImageView image;

		private Part(int id, int duration, float x, float y,
				AnimationListener listener, ImageView frame) {
			this.duration = duration;
			this.x = x;
			this.y = y;
			this.listener = listener;
			this.drawableId = id;
			this.image = frame;

		}

		void setInvisible() {
			image.setVisibility(ImageView.INVISIBLE);
			image.setImageResource(0);
			image.clearAnimation();
		}

		void showImage() {
			image.setImageResource(drawableId);
			image.setVisibility(ImageView.VISIBLE);
		}

		void start() {
			AnimationSet set = new AnimationSet(false);

			Animation aniMove = new TranslateAnimation(0, x, 0, y);
			aniMove.setDuration(duration);
			aniMove.setAnimationListener(listener);
			set.addAnimation(aniMove);

			image.startAnimation(set);

		}
	}

	interface RoundListener {
		public void onRoundChanged(int round);

	}

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

			long previousLeavingMs = 0;

			InstanceState previousState = InstanceState.NULL;
			// Read old session status
			SharedPreferences pref = getPreferences(0);
			logSharedPreferences();

			animationController.reset(pref);

			// Previous session: Timer was runnning
			if (pref.getString(InstanceStateKey.PREVIOUS_STATE_KEY.name(), "")
					.equals(InstanceState.RUNNING.name())) {
				previousState = InstanceState.RUNNING;
			}
			// Previous session: Timer was stopped
			else if (pref.getString(InstanceStateKey.PREVIOUS_STATE_KEY.name(),
					"").equals(InstanceState.STOPPED.name())) {
				previousState = InstanceState.STOPPED;
				previousLeavingMs = pref.getLong(
						InstanceStateKey.LEAVING_MS.name(), 0);
			}
			// First Session or returned from Settings
			else {
				previousState = InstanceState.NULL;
			}

			// Sound for finish notification
			timerService.setSound(settings.getRingtone());

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

				// Brush at the end not used: Reset
				// Same happens like visible finished timer
				if (previousLeavingMs == 0) {
					Log.v(TAG, "LeavingMs=0: reset");
					timerService.setLeavingMs(settings.getDurationMs());
					resetBtn.setEnabled(false);
					startBtn.setEnabled(true);
					animationController.reset(null);

				}

				// Timer was running at last activity stop but meanwhile has
				// finished
				else if (previousState.equals(InstanceState.RUNNING)) {
					Log.v(TAG, "Previous state=running");
					timerService.setLeavingMs(0);
					resetBtn.setEnabled(true);
					startBtn.setEnabled(false);

				}
				// Last Session closed with a stopped timer
				else if (previousState.equals(InstanceState.STOPPED)) {
					Log.v(TAG, "Previous state=stopped");
					timerService.setLeavingMs(previousLeavingMs);
					resetBtn.setEnabled(true);
					startBtn.setEnabled(true);
				}
				// New Session
				else {
					Log.v(TAG, "No previous state");
					timerService.setLeavingMs(settings.getDurationMs());
					resetBtn.setEnabled(false);
					startBtn.setEnabled(true);
				}

				startBtn.setText(R.string.startButtonStart);
				animationController.stop();
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
					if (pLeavingMs == 0) {
						startBtn.setText(R.string.startButtonStart);
						timerService.setLeavingMs(settings.getDurationMs());
						animationController.stop();
						animationController.reset(null);
						resetBtn.setEnabled(false);
						startBtn.setEnabled(true);
						refreshTimerView();
						refreshDurationView();
					} else {
						startBtn.setText(R.string.startButtonStart);
						animationController.stop();
						resetBtn.setEnabled(true);
						refreshTimerView();
					}
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

	private void logSharedPreferences() {
		SharedPreferences sp = getPreferences(0);

		if (!sp.contains(InstanceStateKey.PREVIOUS_STATE_KEY.name())) {
			Log.v(TAG, "logSharedPrefefences(): "
					+ InstanceStateKey.PREVIOUS_STATE_KEY.name() + " not found");
		} else {
			Log.v(TAG,
					"logSharedPreferences(): "
							+ InstanceStateKey.PREVIOUS_STATE_KEY.name()
							+ "="
							+ sp.getString(
									InstanceStateKey.PREVIOUS_STATE_KEY.name(),
									"NULL"));

		}
		if (!sp.contains(InstanceStateKey.LEAVING_MS.name())) {
			Log.v(TAG,
					"logSharedPrefefences(): "
							+ InstanceStateKey.LEAVING_MS.name() + " not found");
		} else {
			Log.v(TAG,
					"logSharedPreferences(): "
							+ InstanceStateKey.LEAVING_MS.name()
							+ "="
							+ sp.getLong(InstanceStateKey.LEAVING_MS.name(), -1));
		}
		animationController.logSharedPreferences();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);

		// Settings might be changed
		if (requestCode == SHOW_PREFERENCES) {

			// Settings can be changed from settings menu: init here and in
			// onCreate()
			settings = new Settings(this);

			SharedPreferences.Editor editor = getPreferences(0).edit();

			// Instantiate new ac because of possible changed speed
			animationController = new AnimationController(settings.getSpeed());
			animationController.storeState(editor);

			editor.putString(InstanceStateKey.PREVIOUS_STATE_KEY.name(),
					InstanceState.NULL.name());

			boolean commitResult = editor.commit();
			Log.v(TAG, "SharedPreferences.commit=" + commitResult);
			logSharedPreferences();

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

	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// Log.v(TAG, "onConfigurationChanged()");
	// super.onConfigurationChanged(newConfig);
	// }

	@Override
	public void onCreate(Bundle savedInstanceStatexx) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceStatexx);
		setContentView(R.layout.activity_main);

		// For very first call: Initialize preferences. Otherwise: do nothing
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		roundView = (TextView) findViewById(R.id.round);

		// Will also be set in onStart()
		settings = new Settings(this);

		progressBar = (ProgressBar) findViewById(R.id.progress_bar);

		timerView = (TextView) findViewById(R.id.count_down);

		animationController = new AnimationController(settings.getSpeed());

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

					timerService.start();

					// Keep screen on while view is visible
					// WindowManager wm = (WindowManager)
					// getSystemService(Context.WINDOW_SERVICE);
					// wm.updateViewLayout(v, new WindowManager.LayoutParams(
					// WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));

				}

			}

		});

		resetBtn = (Button) findViewById(R.id.resetBtn);
		resetBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				reset();
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
			if (timerService != null)
				timerService.stop();

			startActivityForResult(new Intent(this, Pref.class),
					SHOW_PREFERENCES);

			return true;

		}
		return false;
	}

	@Override
	protected void onStart() {
		Log.v(TAG, "onStart()");
		super.onStart();

		progressBar.setMax((int) settings.getDurationMs());
		refreshDurationView();

		// bind and start the service (maybe it's still running but unbound
		doBindService();

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "onStop()");

		SharedPreferences.Editor editor = getPreferences(0).edit();
		if (animationController != null) {
			animationController.storeState(editor);
		}
		if (timerService.isRunning()) {
			editor.putString(InstanceStateKey.PREVIOUS_STATE_KEY.name(),
					InstanceState.RUNNING.name());

		} else {
			editor.putString(InstanceStateKey.PREVIOUS_STATE_KEY.name(),
					InstanceState.STOPPED.name());
			editor.putLong(InstanceStateKey.LEAVING_MS.name(),
					timerService.getLeavingMs());
		}

		boolean commitResult = editor.commit();
		Log.v(TAG, "SharedPreferences.commit=" + commitResult);
		logSharedPreferences();

		doUnbindService();

	}

	private void refreshDurationView() {
		// Update duration setting in view
		Date endTime = new Date(settings.getDurationMs());
		((TextView) findViewById(R.id.end_time)).setText(String.format(
				DURATION_FORMAT, endTime));
	}

	private void refreshTimerView() {
		progressBar.setProgress((int) (settings.getDurationMs() - timerService
				.getLeavingMs()));
		date.setTime(timerService.getLeavingMs());
		timerView.setText(String.format(DURATION_FORMAT, date));
	}

	private void reset() {
		Log.v(TAG, "reset()");

		timerService.setLeavingMs(settings.getDurationMs());
		refreshTimerView();
		animationController.reset(null);
		startBtn.setEnabled(true);
		resetBtn.setEnabled(false);
		refreshDurationView();

	}
}

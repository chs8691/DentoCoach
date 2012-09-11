package de.cs.android.dentocoach;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.cs.android.dentocoach.settings.SettingValues;
import de.cs.android.dentocoach.settings.SettingsActivity;
import de.cs.android.dentocoach.settings.SettingsFactory;

public class DentoCoach extends Activity {
	class AnimationController implements AnimationListener {

		private Speed speed;

		private RoundListener roundListener = null;
		private int pos;
		private boolean run = true;
		private short round = 0;
		private Part actPart = null;

		private final List<Part> partList = new ArrayList<Part>();

		public AnimationController(Speed speed) {

			setSpeed(speed);
			builder();
			reset();

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

		// private void builderOld() {
		// // upper out
		// partList.add(new Part(R.id.brush_ulo, getPartDuration(), 20, -40,
		// this));
		// partList.add(new Part(R.id.brush_ufo, getPartDuration(), 40, 0,
		// this));
		// partList.add(new Part(R.id.brush_uro, getPartDuration(), 20, 40,
		// this));
		//
		// // lower out
		// partList.add(new Part(R.id.brush_lro, getPartDuration(), -20, 40,
		// this));
		// partList.add(new Part(R.id.brush_lfo, getPartDuration(), -40, 0,
		// this));
		// partList.add(new Part(R.id.brush_llo, getPartDuration(), -20, -40,
		// this));
		//
		// // lower in
		// partList.add(new Part(R.id.brush_lli, getPartDuration(), 20, 40,
		// this));
		// partList.add(new Part(R.id.brush_lfi, getPartDuration(), 40, 0,
		// this));
		// partList.add(new Part(R.id.brush_lri, getPartDuration(), 20, -40,
		// this));
		//
		// // upper in
		// partList.add(new Part(R.id.brush_uri, getPartDuration(), -20, -40,
		// this));
		// partList.add(new Part(R.id.brush_ufi, getPartDuration(), -40, 0,
		// this));
		// partList.add(new Part(R.id.brush_uli, getPartDuration(), -20, 40,
		// this));
		//
		// // upper top
		// partList.add(new Part(R.id.brush_ult, getPartDuration(), 20, -40,
		// this));
		// partList.add(new Part(R.id.brush_uft, getPartDuration(), 40, 0,
		// this));
		// partList.add(new Part(R.id.brush_urt, getPartDuration(), 20, 40,
		// this));
		//
		// // lower top
		// partList.add(new Part(R.id.brush_lrt, getPartDuration(), -20, 40,
		// this));
		// partList.add(new Part(R.id.brush_lft, getPartDuration(), -40, 0,
		// this));
		// partList.add(new Part(R.id.brush_llt, getPartDuration(), -20, -40,
		// this));
		//
		// }

		private int getPartDuration() {
			return (speed.value() * 1000);
		}

		public short getRound() {
			return round;
		}

		private void goOn() {
			if (!run)
				run = true;
			next();
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

		/** After pause go on with same step */
		private void pause() {
			run = false;
			pos--;
		}

		public void reset() {
			pos = 0;
			run = false;
			round = 0;

			// Clear last viewed brush
			if (actPart != null)
				actPart.setInvisible();

			// Show start brush
			actPart = partList.get(pos);
			actPart.showImage();

		}

		void setRoundListener(RoundListener roundListener) {
			this.roundListener = roundListener;
		}

		public void setSpeed(Speed speed) {
			this.speed = speed;
		}

	}

	class Part {
		private final int id;
		private final int duration;
		private final float x;
		private final float y;
		private final AnimationListener listener;
		private final ImageView i;

		private Part(int id, int duration, float x, float y,
				AnimationListener listener) {
			this.id = id;
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

	interface RoundListener {
		public void onRoundChanged(short round);
	}

	/**
	 * Status of the brush automate
	 * 
	 * @author ChristianSchulzendor
	 * 
	 */
	enum Status {
		RUN, PAUSE, NULL;
	}

	private static final String TAG = "DentoCoachActivity";

	private Button startBtn = null;

	private Button resetBtn = null;
	private ProgressBar progressBar = null;
	private Chronometer chronometer = null;
	private TextView roundView = null;
	long runningTime = 0;
	private Status status = Status.NULL;
	private AnimationController animationController = null;
	private MediaPlayer mp;

	private static final int DIALOG_ABOUT_ID = 1;
	private static final int DIALOG_SETTINGS_ID = 2;

	private Context mContext;

	private SettingValues settings;

	private void init() {

		Log.v(TAG, "Entering init()");
		roundView = (TextView) findViewById(R.id.round);
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		chronometer = (Chronometer) findViewById(R.id.chronometer);

		settings = SettingsFactory.createSettingValues(this);

		progressBar.setMax(settings.getDuration() * 60);

		chronometer
				.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

					public void onChronometerTick(Chronometer chronometer) {
						runningTime = SystemClock.elapsedRealtime()
								- chronometer.getBase();
						if (runningTime <= (settings.getDuration() * 1000 * 60)) {
							progressBar.setProgress((int) (runningTime / 1000));
						} else {
							chronometer.stop();
							chronometer.setBackgroundColor(Color.GREEN);

							status = Status.NULL;
							startBtn.setText(R.string.startButtonRun);
							progressBar.setProgress(0);
							animationController.pause();
							// mp.stop();

						}

					}
				});

		animationController = new AnimationController(settings.getSpeed());
		animationController.setRoundListener(new RoundListener() {

			public void onRoundChanged(short round) {
				roundView.setText(String.valueOf(round));
			}

		});

		// mp = MediaPlayer.create(this, R.raw.schuften);
		// mp.setLooping(true);

		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				switch (status) {
				case NULL:
					progressBar.setProgress(0);
					startBtn.setText(R.string.startButtonPause);
					chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();
					chronometer.setBackgroundColor(Color.BLACK);
					status = Status.RUN;
					animationController.goOn();
					resetBtn.setEnabled(false);
					// mp.start();
					break;
				case PAUSE:
					startBtn.setText(R.string.startButtonPause);
					chronometer.setBase(SystemClock.elapsedRealtime()
							- runningTime);
					chronometer.start();
					status = Status.RUN;
					animationController.goOn();
					resetBtn.setEnabled(false);
					// mp.start();
					break;
				case RUN:
					startBtn.setText(R.string.startButtonRun);
					chronometer.stop();
					status = Status.PAUSE;
					animationController.pause();
					resetBtn.setEnabled(true);
					// mp.pause();
					break;
				}

			}
		});

		resetBtn = (Button) findViewById(R.id.resetBtn);
		resetBtn.setEnabled(false);
		resetBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				reset();

			}

		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		System.out.println("----> onCreateDialog" + id);

		switch (id) {
		case DIALOG_ABOUT_ID:
			return showAbout();

		case (DIALOG_SETTINGS_ID):

		default:
			break;
		}

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dento_coac_menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		if (mp != null) {
			// mp.release();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("----> Entering onOptionsItemSelected for Item="
				+ item.getTitle() + " " + item.getItemId());
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.about:
			System.out.println("----> Calling showDialog(about)");
			showDialog(DIALOG_ABOUT_ID);
			return true;

		case R.id.settings:
			System.out.println("----> Calling showSettings()");
			reset();
			showSettings();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		init();
		reset();
		super.onResume();
	}

	private void reset() {
		chronometer.stop();
		chronometer.setBase(SystemClock.elapsedRealtime());
		status = Status.NULL;
		startBtn.setText(R.string.startButtonRun);
		progressBar.setProgress(0);
		animationController.reset();
	}

	private Dialog showAbout() {

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog,
				(ViewGroup) findViewById(R.id.about_dialog_layout_root));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		AlertDialog about = builder.create();

		return about;

	}

	private void showSettings() {

		// Starts activity
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private void testIntent() {
		Intent intent = new Intent(
				android.media.RingtoneManager.ACTION_RINGTONE_PICKER);

	}

}
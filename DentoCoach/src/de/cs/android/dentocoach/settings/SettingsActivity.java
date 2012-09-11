package de.cs.android.dentocoach.settings;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import de.cs.android.dentocoach.R;
import de.cs.android.dentocoach.Speed;

public class SettingsActivity extends ListActivity {

	private static final int DIALOG_ID_SPEED = 1;
	private static final int DIALOG_ID_DURATION = 2;
	private static final int[] TO = { R.id.setting_header,
			R.id.setting_description };
	private static final String HEADER = "header";
	private static final String DESCRIPTION = "description";
	private static final String[] FROM = { HEADER, DESCRIPTION };
	private static final int RC_SET_RINGTONE = 1;

	private final List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();

	private Settings setVal;

	private static final String TAG = "SettingsActivity";

	private String asText(int id) {
		return String.valueOf(getText(id));
	}

	private Dialog callDurationDialog() {
		CharSequence min = " " + getText(R.string.unitMinShort);

		final CharSequence[] items = { "1" + min, "2" + min, "3" + min,
				"4" + min, "5" + min };

		int actItem = -1;

		// Converting
		actItem = setVal.getDuration() - 1;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.settingDurationHeader)
				.setCancelable(true)
				.setSingleChoiceItems(items, actItem,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int item) {

								// Back converting and store in entity
								setVal.setDuration(item + 1);
								updateDataList();
								getListView().invalidateViews();
								dialog.dismiss();
							}
						})
				.setNeutralButton(R.string.ButtonCancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								getListView().invalidateViews();
								dialog.dismiss();

							}
						});

		AlertDialog dialog = builder.create();

		return dialog;
	}

	private Dialog callRingtoneSwitchDialog() {
		System.out.println("-----> callRingtoneSwitchDialog");

		final CharSequence[] items = { getText(R.string.ringtoneSwitchOff),
				getText(R.string.ringtoneSwitchOn) };

		int actItem = -1;

		// Converting
		actItem = setVal.getRingtoneSwitch() ? 1 : 0;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.settingRingtoneSwitchHeader)
				.setCancelable(true)
				.setSingleChoiceItems(items, actItem,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int item) {

								// Back converting and store in entity
								setVal.setRingtoneSwitch(item == 0 ? false
										: true);
								updateDataList();
								getListView().invalidateViews();
								dialog.dismiss();

							}
						})

				.setNeutralButton(R.string.ButtonCancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								getListView().invalidateViews();
								dialog.dismiss();

							}
						});

		AlertDialog dialog = builder.create();

		return dialog;

	}

	private Dialog callSpeedDialog() {
		System.out.println("-----> callSpeedDialog");

		final CharSequence[] items = { getText(R.string.speed_Fast),
				getText(R.string.speed_Medium), getText(R.string.speed_Slow) };

		int actItem = -1;

		// Converting
		actItem = setVal.getSpeed().value() - 1;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.settingDurationHeader)
				.setCancelable(true)
				.setSingleChoiceItems(items, actItem,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int item) {

								// Back converting and store in entity
								setVal.setSpeed(Speed.create(item + 1));
								updateDataList();
								getListView().invalidateViews();
								dialog.dismiss();

							}
						})

				.setNeutralButton(R.string.ButtonCancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								getListView().invalidateViews();
								dialog.dismiss();

							}
						});

		AlertDialog dialog = builder.create();

		return dialog;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == RC_SET_RINGTONE) {
			Uri uri = data
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (uri != null) {
				String ringTonePath = uri.toString();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setVal = new Settings(this);
		updateDataList();
		setListAdapter(new SimpleAdapter(this, dataList,
				R.layout.settings_item, FROM, TO));

		ListView lv = getListView();

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					showDialog(DIALOG_ID_DURATION);
					break;
				case 1:
					showDialog(DIALOG_ID_SPEED);
					break;
				case 2:
					break;
				case 3:
					// Call ringtone picker ACTION_RINGTONE_PICKER
					Log.v(TAG, "Calling ringtone picker");
					String uri = null; // TODO: Old value
					Intent intent = new Intent(
							RingtoneManager.ACTION_RINGTONE_PICKER);
					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
							RingtoneManager.TYPE_RINGTONE);
					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
							"Select final jingle");
					if (uri == null) {
						intent.putExtra(
								RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
								(Uri) null);
					} else {
						intent.putExtra(
								RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
								Uri.parse(uri));
					}

					startActivityForResult(intent, RC_SET_RINGTONE);
					break;
				default:
					break;
				}
			}
		});
		System.out.println("<----- SettingsActivity");

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ID_DURATION:
			return callDurationDialog();
		case DIALOG_ID_SPEED:
			return callSpeedDialog();
		default:
			throw new InvalidParameterException();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	private void updateDataList() {
		dataList.clear();
		Map<String, String> dataEntry = new HashMap<String, String>();
		dataEntry.put(HEADER, asText(R.string.settingDurationHeader));
		dataEntry.put(DESCRIPTION, asText(R.string.settingDurationDescription)
				+ " " + setVal.getDuration() + " "
				+ asText(R.string.unitMinLong));
		dataList.add(dataEntry);

		Map<String, String> dataEntry2 = new HashMap<String, String>();
		String speedText;
		switch (setVal.getSpeed()) {
		case SLOW:
			speedText = asText(R.string.speed_Slow);
			break;
		case MEDIUM:
			speedText = asText(R.string.speed_Medium);
			break;
		case FAST:
			speedText = asText(R.string.speed_Fast);
			break;
		default:
			throw new IllegalStateException();
		}
		dataEntry2.put(HEADER, asText(R.string.settingSpeedHeader));
		dataEntry2.put(DESCRIPTION, asText(R.string.settingSpeedDescription)
				+ " " + speedText);
		dataList.add(dataEntry2);

		String ringtoneSwitchText;
		Map<String, String> dataEntry3 = new HashMap<String, String>();
		dataEntry3.put(HEADER, asText(R.string.settingRingtoneSwitchHeader));
		if (setVal.getRingtoneSwitch())
			ringtoneSwitchText = asText(R.string.ringtoneSwitchOn);
		else
			ringtoneSwitchText = asText(R.string.ringtoneSwitchOff);

		dataEntry3.put(DESCRIPTION,
				asText(R.string.settingRingtoneSwitchDescription) + " "
						+ ringtoneSwitchText);
		dataList.add(dataEntry3);

		String ringtoneText;
		Map<String, String> dataEntry4 = new HashMap<String, String>();
		dataEntry4.put(HEADER, asText(R.string.settingRingtoneHeader));
		ringtoneText = setVal.getRingtone();

		dataEntry4.put(DESCRIPTION, asText(R.string.settingRingtoneDescription)
				+ " " + ringtoneText);
		dataList.add(dataEntry4);

	}

}

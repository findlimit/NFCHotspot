package tw.edu.ntu.nfchotspot;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class NFCReceiveActivity extends Activity {
	private String SSID;
	private String PW;
	int flagCanGo = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("nfc", "create");
		resolveIntent(getIntent());

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e("nfc", "resume");
		Log.e("flag", flagCanGo + "");
		if (flagCanGo == 1) {

			flagCanGo = 0;
			finish();

		} else if (flagCanGo == -1) {
			flagCanGo = -2;
			goStartIntent();

		} else {
			finish();
		}
		// if (flagNfcStart == true) {

		// finish();
		// }

	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.e("nfc", "newIntent");
		super.onNewIntent(intent);
		if (flagCanGo != -2) {
			flagCanGo = 1;
		} else {
			flagCanGo = -1;
		}
	}

	void resolveIntent(Intent intent) {
		// Parse the intent
		Log.e("nfc", "1");
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Log.e("nfc", "2");
			flagCanGo = -1;
			// When a tag is discovered we send it to the service to be save.
			// We include a PendingIntent for the service to call back onto.
			// This will cause this activity to be restarted with onNewIntent().
			// At that time we read it from the database and view it.z

			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs;
			if (rawMsgs != null) {
				Log.e("nfc", "3");
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				Log.e("nfc", "4");
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
			// Setup the views
			getSSIDandPW(msgs);
			// goStartIntent();
		} else {
			Log.e("nfc", "5");
			Intent i = new Intent();
			finish();
			// startActivity(i);
		}

	}

	// Get SSID and PW from NFC msg.
	private void getSSIDandPW(NdefMessage[] msgs) {
		SSID = new String(msgs[0].getRecords()[0].getPayload());
		// Toast.makeText(NFCReceiveActivity.this, SSID,
		// Toast.LENGTH_SHORT).show();
		PW = new String(msgs[0].getRecords()[1].getPayload());
		// Toast.makeText(NFCReceiveActivity.this, PW,
		// Toast.LENGTH_SHORT).show();

		// Set to global SSID PW
		Global.SERVER_SSID = "\"" + SSID + "\"";
		Global.SERVER_KEY = "\"" + PW + "\"";
		Global.flagFromNFC = true;

	}

	// Do something after receive the NFC TAG
	private void goStartIntent() {
		Intent intent = new Intent();
		intent.setClass(this, ClientActivity.class);
		startActivity(intent);
		// finish();
	}
}
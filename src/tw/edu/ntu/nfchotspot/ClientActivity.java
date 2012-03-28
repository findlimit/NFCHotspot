package tw.edu.ntu.nfchotspot;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ClientActivity extends Activity {

	private NfcAdapter mAdapter;
	private NdefMessage mMessage;
	Thread nfc_t;
	WifiManager mWifiManager;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			}
		}
	};
	private Thread clientThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.e("bug", "comeInBPA " + Global.flagIsPlaying);
		Global.flagIsPlaying = true;
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		connect();
		sendNFCMsg();

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("re", "re");
	}

	private void sendNFCMsg() {
		try {

			mAdapter = NfcAdapter.getDefaultAdapter(ClientActivity.this);

			// 消除引號
			String mSSID = Global.SERVER_SSID.substring(1,
					Global.SERVER_SSID.length() - 1);
			String mPW = Global.SERVER_KEY.substring(1,
					Global.SERVER_KEY.length() - 1);

			byte[] byteSSID = mSSID.getBytes();
			byte[] bytePW = mPW.getBytes();
			mMessage = new NdefMessage(
					new NdefRecord[] {
							new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
									"text/nfchotspot".getBytes(), new byte[] {},
									byteSSID),
							new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
									"text/nfchotspot".getBytes(), new byte[] {},
									bytePW) });
		} catch (java.lang.NoClassDefFoundError e) {
			Toast.makeText(this, "No NFC Device", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onPause() {
		if (mAdapter != null)
			mAdapter.disableForegroundNdefPush(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null) {
			mAdapter.enableForegroundNdefPush(this, mMessage);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// WifiManager mWifiManager = (WifiManager)
		// getSystemService(WIFI_SERVICE);
		// mWifiManager.disconnect();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.e("bp", "bp");

		mWifiManager.disconnect();
		super.onBackPressed();
		finish();
	}

	protected void connect() {
		nfc_t = new Thread() {
			@Override
			public void run() {

				Message m = mHandler.obtainMessage(Global.SHOW_MSG);
				mHandler.sendMessage(m);

				turnOnWifi();

				openClientConnection();

			}
		};
		nfc_t.start();
	}

	protected void openClientConnection() {
		Log.e(Global.TAG, "+openClientConnection()");

		DhcpInfo mDhcpInfo;
		int ipadd;

		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (Global.flagFromNFC) {
				connectToServer();
			}
			mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			mDhcpInfo = mWifiManager.getDhcpInfo();

			ipadd = mDhcpInfo.gateway;
			Global.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF)
					+ "." + (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
			Log.v(Global.TAG, Global.SERVER_IP);
			clientThread = new Thread(new ClientThread(this));
			clientThread.start();
			break;
		}

		Log.e(Global.TAG, "-openClientConnection()");
	}

	private void connectToServer() {

		List<WifiConfiguration> mConfigs = mWifiManager.getConfiguredNetworks();

		boolean inConfig = false;
		for (WifiConfiguration config : mConfigs) {
			if (config.SSID.equals(Global.SERVER_SSID)
					|| config.SSID.equals("\"" + Global.SERVER_SSID + "\"")) {
				inConfig = true;
				mWifiManager.enableNetwork(config.networkId, true);
				Log.d(Global.TAG, config.toString());
				break;
			}
		}

		if (!inConfig) {
			Log.v(Global.TAG, "not it config");
			WifiConfiguration netConfig = new WifiConfiguration();
			netConfig.SSID = Global.SERVER_SSID;
			// netConfig.preSharedKey = Global.SERVER_KEY;
			netConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

			if (mWifiManager.enableNetwork(mWifiManager.addNetwork(netConfig),
					true))
				Log.v(Global.TAG, "IP obtain ok");
			else
				Log.v(Global.TAG, "IP obtain failed");
		}
	}

	private void turnOnWifi() {
		Log.e(Global.TAG, "+turnOnWifi()");

		// open ordinary wifi connection
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}

		Log.e(Global.TAG, "-turnOnWifi()");
	}
	
	public Handler getHandler() {
    	return mHandler;
    }
}
package tw.edu.ntu.nfchotspot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
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

public class NFC_HotSpotActivity extends Activity {
	
	private NfcAdapter mAdapter;
	private NdefMessage mMessage;
	private String mSSID;
	private String mPW;
	
	Thread socketListener_t;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			}
		}
	};
	private InputStream imageInputStream;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initVariables();
//		setListeners();
		turnOnWifiAP();
		openServerConnection();
		sendNFCMsg();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        Intent intent = getIntent();
        String action = intent.getAction();
        
        // Check to see that the Activity started due to an Android Beam
    	if (Intent.ACTION_SEND.equals(action)) {
    		setImageData(intent);
    	}
    }
    
    private void setImageData(Intent intent) {
    	Bundle extras = intent.getExtras();
    	if (extras.containsKey(Intent.EXTRA_STREAM)) {
                                                                       
    		// Get resource path from intent callee
    		Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                                                                       
    		// Query gallery for camera picture via
    		// Android ContentResolver interface
    		ContentResolver cr = getContentResolver();
    		try {
    			imageInputStream = cr.openInputStream(uri);
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		// Get binary bytes for encode
    		// mImagedata = Utility.getBytesFromInputStream(is);
                                                                       
    		// TODO send image from bluetooth
    		// SendRequest(data_string);
    	}
                                                                       
    }
    
    private void initVariables() {
		try {
			mAdapter = NfcAdapter.getDefaultAdapter(NFC_HotSpotActivity.this);
		} catch (NoClassDefFoundError e) {
		}
		// Produce the SSID and PW
		mSSID = "BombPlus";// + (int) (Math.random() * 2 + 1);
		mPW = "BombPlus" + (int) (Math.random() * 100 + 1);

		if (mAdapter != null) {
			Toast.makeText(this, "have NFC", Toast.LENGTH_SHORT).show();
		} else {

			Toast.makeText(this, "have no NFC", Toast.LENGTH_SHORT).show();
		}
		Global.SERVER_SSID = mSSID;
		// Global.SERVER_KEY = mPW;
	}
    
    void clearHost() {
		socketListener_t.stop();
		turnOffWifiAP();
	}

	private void sendNFCMsg() {
		try {

			Log.e("nfc", mSSID + " " + mPW);
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
	
	private void openServerConnection() {
		Log.i(Global.TAG, "+createServer()");
		socketListener_t = new Thread(new SocketListener(this,
				Global.SERVER_PORT));
		socketListener_t.start();
		Log.i(Global.TAG, "-createServer()");
	}

	private void turnOffWifiAP() {
		WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

		// turn on wifi ap using reflection
		Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
		for (Method method : wmMethods) {
			if (method.getName().equals("setWifiApEnabled")) {
				WifiConfiguration netConfig = new WifiConfiguration();
				netConfig.SSID = Global.SERVER_SSID;
				netConfig.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.NONE);
				netConfig.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);

				try {
					method.invoke(mWifiManager, netConfig, false);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		// close ordinary wifi connection
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	private void turnOnWifiAP() {
		if (mAdapter != null) {
			WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

			// close ordinary wifi connection
			if (mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(false);
			}

			// turn on wifi ap using reflection
			Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
			for (Method method : wmMethods) {
				if (method.getName().equals("setWifiApEnabled")) {
					WifiConfiguration netConfig = new WifiConfiguration();
					netConfig.SSID = Global.SERVER_SSID;
					netConfig.allowedAuthAlgorithms
							.set(WifiConfiguration.AuthAlgorithm.OPEN);
					netConfig.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.NONE);

					try {
						method.invoke(mWifiManager, netConfig, true);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}

			DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();
			Log.e(Global.TAG, mDhcpInfo.toString());
		}
	}
	
	public Handler getHandler() {
    	return mHandler;
    }
    
    public InputStream getImageStream() {
    	return imageInputStream;
    }
}
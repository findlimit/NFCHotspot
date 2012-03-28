package tw.edu.ntu.nfchotspot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

class ClientThread extends Thread {

	private BluetoothAdapter mBTAdapter;
	private Socket mmSocket;

	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private ClientActivity activity;
	private Handler handler;

	public ClientThread(ClientActivity _Activity) {
		this.handler = _Activity.getHandler();
		this.activity = _Activity;

		try {
			mmSocket = new Socket(Global.SERVER_IP, Global.SERVER_PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		final File f = new File(Environment.getExternalStorageDirectory() + "/"
				+ NFC_HotSpotActivity.class.getName() + "/wifip2pshared-"
				+ System.currentTimeMillis() + ".jpg");

		File dirs = new File(f.getParent());

		if (!dirs.exists())
			dirs.mkdirs();
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Log.d(NFCBluetoothActivity.TAG, "server: copying files " +
		// f.toString());
		InputStream inputstream = null;
		try {
			inputstream = mmSocket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Utility.copyFile(inputstream, new FileOutputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Message viewImage = new Message();
		Bundle data = new Bundle();
		data.putString("type", "succeed");
		data.putString("file_uri", f.getAbsolutePath());

		viewImage.setData(data);
		handler.sendMessage(viewImage);

		// be careful
		Message msg2 = new Message();
		msg2.what = Global.CHANGE_MSG;
		msg2.obj = "server socket close\n";
		handler.sendMessage(msg2);

	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
		}
	}
}

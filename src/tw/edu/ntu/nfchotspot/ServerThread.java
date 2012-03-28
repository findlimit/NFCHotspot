package tw.edu.ntu.nfchotspot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

class ServerThread extends Thread {
	private final BluetoothServerSocket serverSocket;
	private BluetoothAdapter mBTAdapter;
	private Handler handler;
	private InputStream imageDataIS;
	private Socket clientSocket;

	private static final String NAME = "NFCBluetooth";
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	private static NFC_HotSpotActivity activity;
	
	public ServerThread(NFC_HotSpotActivity _Activity, Socket cSocket) {
		this.mBTAdapter = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();
		this.activity = _Activity;
		this.handler = _Activity.getHandler();
		this.imageDataIS = _Activity.getImageStream();
		this.clientSocket = cSocket;

		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		BluetoothServerSocket tmp = null;
		try {
			// MY_UUID is the app's UUID string, also used by the client code
			tmp = mBTAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverSocket = tmp;

		Message msg = new Message();
		msg.obj = "client connect succeed";
		msg.what = Global.ADD_CLIENT;
		handler.sendMessage(msg);

	}

	public void run() {
		try {
			OutputStream stream = null;
			stream = clientSocket.getOutputStream();
			Utility.copyFile(imageDataIS, stream);
			
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Will cancel the listening socket, and cause the thread to finish */
	public void cancel() {
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}
}
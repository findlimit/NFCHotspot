package tw.edu.ntu.nfchotspot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

class SocketListener implements Runnable {
	
	private int serverPort;
	private NFC_HotSpotActivity activity;
	private Handler handler;

	public SocketListener(NFC_HotSpotActivity _Activity, int port) {
		// TODO Auto-generated constructor stub
		activity = _Activity;
		handler = _Activity.getHandler();
		serverPort = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i(Global.TAG, "+SocketListener()");
		try {
			// establish server socket
			int connIndex = 0;
			ServerSocket serverSocket = new ServerSocket(serverPort);
			Log.i(Global.TAG, "port:" + serverSocket.getLocalPort());

			while (true) {
				Log.e(Global.TAG, "WAIT FOR CONNECTION");
				Socket incoming = serverSocket.accept();
				Log.e(Global.TAG, "Connected a client!connIndex:"
								+ connIndex
								+ " RemoteSocketAddress:"
								+ String.valueOf(incoming.getRemoteSocketAddress()));
				Thread serverthread = new Thread(new ServerThread(activity, incoming));
				serverthread.start();
				connIndex++;
				Log.e("cln",connIndex+"");
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(Global.TAG, "-SocketListener()");
		
	}
}




package com.bluetoothdemo.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.bluetoothdemo.ServerActivity;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * 监听蓝牙连接线程
 * 作者： 代码来自于Google官方 -> API指南 -> 蓝牙模块
 * 日期： 18/12/14
 */

public class AcceptThread extends Thread {

    private static final String TAG = "BluetoothDemo";

    private final BluetoothServerSocket mmServerSocket;

    private AcceptCallBack callBack;

    public AcceptThread(BluetoothAdapter bluetoothAdapter, AcceptCallBack callBack) {

        this.callBack = callBack;

        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("bluetoothdemo", UUID
                    .fromString(ServerActivity.uuidStr));
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            Log.i(TAG, "AcceptThread监听中...");

            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {

                try {
                    // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket); //启动数据传输的线程
                    if(callBack != null) {
                        callBack.onAcceptSucceed(socket);
                    }


                    Log.i(TAG, "AcceptThread连接成功");
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
        }
    }

    public interface AcceptCallBack {
        public void onAcceptSucceed(BluetoothSocket serverSocket);
    }

}

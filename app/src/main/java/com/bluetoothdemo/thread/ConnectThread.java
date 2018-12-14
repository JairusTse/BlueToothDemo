package com.bluetoothdemo.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.bluetoothdemo.ServerActivity;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * 发起蓝牙连接的线程
 * 作者： 代码来自于Google官方 -> API指南 -> 蓝牙模块
 * 日期： 18/12/14
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private ConnectCallBack callBack;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter, ConnectCallBack callBack) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = bluetoothAdapter;
        this.callBack = callBack;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(ServerActivity.uuidStr));
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
//        manageConnectedSocket(mmSocket); //启动数据传输的线程
        if(callBack != null) {
            callBack.onConnectSucceed(mmSocket);
        }

    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public interface ConnectCallBack {
        public void onConnectSucceed(BluetoothSocket serverSocket);
    }
}

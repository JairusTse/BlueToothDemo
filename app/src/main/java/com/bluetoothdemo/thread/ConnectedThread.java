package com.bluetoothdemo.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.bluetoothdemo.ServerActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 发送和接收数据
 * 作者： 代码来自于Google官方 -> API指南 -> 蓝牙模块
 * 日期： 18/12/14
 */

public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                if(mHandler != null) {
                    mHandler.obtainMessage(ServerActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                }

            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

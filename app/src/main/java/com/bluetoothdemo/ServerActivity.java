package com.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bluetoothdemo.thread.AcceptThread;
import com.bluetoothdemo.thread.ConnectedThread;
import com.bluetoothdemo.utils.PermissionUtil;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener, AcceptThread
        .AcceptCallBack {

    private static final String TAG = "BluetoothDemo";

    private TextView tv;
    private TextView tvAccept;

    private BluetoothAdapter mBluetoothAdapter;

    //自己生成的uuid
    public static String uuidStr = "7d9272e4-820f-42e4-ba53-b8791bb31e95";
    public static final int MESSAGE_READ = 100;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) msg.obj;
                    tv.setText(new String(buffer));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //绑定控件
        initView();
        //注册广播接收器
        registerReceiver();

    }

    private void initView() {
        tv = (TextView) findViewById(R.id.tv);
        tvAccept = (TextView) findViewById(R.id.tv_accept);

        tv.setOnClickListener(this);
        tvAccept.setOnClickListener(this);
    }


    /**
     * 注册广播
     */
    private void registerReceiver() {
        //配对蓝牙广播
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //连接蓝牙广播
        IntentFilter filter6 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        registerReceiver(receiver, filter5);
        registerReceiver(receiver, filter6);
    }

    /**
     * 打开蓝牙
     */
    private void openBlueTooth() {
        if (mBluetoothAdapter == null) {
            //如果mBluetoothAdapter，该设备不支持蓝牙，不过基本上都支持的
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            //若没打开则打开蓝牙
            mBluetoothAdapter.enable();
        }

        tv.setText("AcceptThread监听中...");
        //作为server启动监听线程
        new AcceptThread(mBluetoothAdapter, this).start();
    }

    //广播接收器
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i(TAG, "action = " + action);

            switch (action) {
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                    //配对状态变化广播
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_NONE:
                            Log.i(TAG, "--- 配对失败 ---");
                            tv.setText("配对失败");
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            Log.i(TAG, "--- 配对中... ---");
                            tv.setText("配对中... ");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.i(TAG, "--- 配对成功 ---");
                            tv.setText("配对成功");

                            break;
                    }
                    break;
                }
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                    //连接状态变化广播
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothAdapter.STATE_DISCONNECTED:
                            //未连接
                            Log.i(TAG, "--- 未连接 ---");
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            //连接中
                            Log.i(TAG, "--- 连接中... ---");
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            //连接成功
                            Log.i(TAG, "--- 连接成功 ---");
                            break;
                    }
                }
                break;


            }
        }
    };

    @Override
    public void onAcceptSucceed(BluetoothSocket serverSocket) {
        //server连接成功
        new ConnectedThread(serverSocket, handler).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv:
                //跳转到client页
                startActivity(new Intent(ServerActivity.this, ClientActivity.class));
                break;
            case R.id.tv_accept:
                //要模糊定位权限才能打开到蓝牙
                PermissionUtil.requestEach(this, new PermissionUtil.OnPermissionListener() {
                    @Override
                    public void onSucceed() {
                        //授权成功后打开蓝牙
                        openBlueTooth();
                    }

                    @Override
                    public void onFailed(boolean showAgain) {

                    }
                }, PermissionUtil.LOCATION);
                break;
        }
    }
}

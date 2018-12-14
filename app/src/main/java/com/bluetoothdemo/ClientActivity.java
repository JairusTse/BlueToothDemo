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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothdemo.adapter.ListAdapter;
import com.bluetoothdemo.thread.ConnectThread;
import com.bluetoothdemo.thread.ConnectedThread;
import com.bluetoothdemo.utils.PermissionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener,
        ConnectThread.ConnectCallBack {

    private static final String TAG = "BluetoothDemo";

    private TextView tv;
    private ListView listView;
    private RelativeLayout rlEtView;
    private Button btnSend;
    private EditText et;

    private ListAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private ConnectedThread connectedThread;

    public static final int CONNECT_SUCCEED = 101;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_SUCCEED:
                    rlEtView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //绑定控件
        initView();
        //初始化列表
        initListView();
        //注册广播接收器
        registerReceiver();

    }

    private void initView() {
        tv = (TextView) findViewById(R.id.tv);
        listView = (ListView) findViewById(R.id.listview);
        rlEtView = (RelativeLayout) findViewById(R.id.rl_et_view);
        btnSend = (Button) findViewById(R.id.btn_send);
        et = (EditText) findViewById(R.id.et);

        tv.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    /**
     * 初始化列表
     */
    private void initListView() {
        adapter = new ListAdapter(this, deviceList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //蓝牙配对
                BluetoothDevice device = deviceList.get(position);
                pin(device);
            }
        });
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        //扫描蓝牙广播
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //配对蓝牙广播
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //连接蓝牙广播
        IntentFilter filter6 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        registerReceiver(receiver, filter1);
        registerReceiver(receiver, filter2);
        registerReceiver(receiver, filter3);
        registerReceiver(receiver, filter5);
        registerReceiver(receiver, filter6);
    }

    /**
     * 打开蓝牙
     */
    private void openBlueTooth() {
        if (mBluetoothAdapter == null) {
            //如果mBluetoothAdapter，该设备不支持蓝牙，不过基本上都支持的
        }

        if (!mBluetoothAdapter.isEnabled()) {
            //若没打开则打开蓝牙
            mBluetoothAdapter.enable();
        }

        //扫描：经典蓝牙
        mBluetoothAdapter.startDiscovery();

    }

    /**
     * 配对，配对结果通过广播返回
     *
     * @param device
     */
    public void pin(BluetoothDevice device) {
        if (device == null || !mBluetoothAdapter.isEnabled()) {
            return;
        }

        //配对之前把扫描关闭
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        //判断设备是否配对，没有就进行配对
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                Method createBondMethod = device.getClass().getMethod("createBond");
                Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                returnValue.booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //广播接收器
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i(TAG, "action = " + action);

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    //扫描开始
                    Log.i(TAG, "--- 开始扫描 ---");
                    tv.setText("开始扫描...");
                    break;
                case BluetoothDevice.ACTION_FOUND: {
                    //发现蓝牙
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    if (!TextUtils.isEmpty(device.getName())) {
                        Log.i(TAG, "--- 发现了：" + device.getName() + " ---");
                        deviceList.add(device);
                        //更新蓝牙列表
                        adapter.notifyDataSetChanged();
                    }

                }
                break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //扫描结束
                    Log.i(TAG, "--- 扫描完成 ---");
                    tv.setText("扫描完成");

                    break;
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
                            new ConnectThread(device, mBluetoothAdapter, ClientActivity.this)
                                    .start();
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
    public void onConnectSucceed(BluetoothSocket serverSocket) {

        Message msg = new Message();
        msg.what = CONNECT_SUCCEED;
        handler.sendMessage(msg);

        connectedThread = new ConnectedThread(serverSocket, null);
        connectedThread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv:
                //要模糊定位权限才能搜索到蓝牙
                PermissionUtil.requestEach(this, new PermissionUtil.OnPermissionListener() {
                    @Override
                    public void onSucceed() {

                        //每次扫描前清空列表
                        deviceList.clear();
                        adapter.notifyDataSetChanged();

                        //授权成功后打开蓝牙
                        openBlueTooth();
                    }

                    @Override
                    public void onFailed(boolean showAgain) {

                    }
                }, PermissionUtil.LOCATION);
                break;
            case R.id.btn_send:
                String msg = et.getText().toString().trim();
                if (!TextUtils.isEmpty(msg)) {
                    connectedThread.write(msg.getBytes());
                } else {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

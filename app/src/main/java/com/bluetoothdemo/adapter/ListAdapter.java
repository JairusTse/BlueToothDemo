package com.bluetoothdemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetoothdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： xy
 * 日期： 18/12/14
 */

public class ListAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> deviceList = new ArrayList<>();

    public ListAdapter(Context context, List<BluetoothDevice> deviceList) {
        this.mContext = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = deviceList.get(position);

        holder.tvName.setText(device.getName() + " | " + device.getAddress());
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;

        ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }
    }
}


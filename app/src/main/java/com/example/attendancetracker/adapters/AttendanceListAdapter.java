package com.example.attendancetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.attendancetracker.R;
import com.example.attendancetracker.models.AttendanceRecord;
import java.util.List;

public class AttendanceListAdapter extends BaseAdapter {
    private Context context;
    private List<AttendanceRecord> recordList;

    public AttendanceListAdapter(Context context, List<AttendanceRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return recordList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        AttendanceRecord record = recordList.get(position);

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText("Date: " + record.getDate() + " | Status: " + record.getStatus());
        text2.setText("Reason: " + (record.getReasonCategory() != null ? record.getReasonCategory() : "None"));

        return convertView;
    }
}
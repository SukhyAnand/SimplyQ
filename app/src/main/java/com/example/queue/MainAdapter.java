package com.example.queue;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Integer> busesID;
    private ArrayList<String> busesFromDestination;
    private ArrayList<String> busesToDestination;
    private ArrayList<String> busesName;
    private ArrayList<Integer> busesImage;

    public MainAdapter (Context c, ArrayList<String> busesName, ArrayList<Integer> busesImage, ArrayList<String> busesFromDestination, ArrayList<String> busesToDestination, ArrayList<Integer> busesID) {
        context = c;
        this.busesID = new ArrayList<>(busesID);
        this.busesName = new ArrayList<>(busesName);
        this.busesImage = new ArrayList<>(busesImage);
        this.busesFromDestination = new ArrayList<>(busesFromDestination);
        this.busesToDestination = new ArrayList<>(busesToDestination);
        //Log.i("LAWDA", this.busesID.get(0).toString());
        //Log.i("LAWDAIMAGE", this.busesImage.get(0).toString());
    }

    @Override
    public int getCount() {
        return busesName.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_item,null);
        }


        ImageView imageView = convertView.findViewById(R.id.image_view);
        TextView textView = convertView.findViewById(R.id.text_view);

        Log.i("Position", Integer.toString(position));

        imageView.setImageResource(busesImage.get(position));
        textView.setText(busesName.get(position));
        return convertView;
    }
}

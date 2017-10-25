package com.memeticame.memeticame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by ESTEBANFML on 24-10-2017.
 */

public class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<File> pathsList;

    public GalleryAdapter(Context context, ArrayList<File> pathsList) {
        this.context = context;
        this.pathsList = pathsList;
    }

    @Override
    public int getCount() {
        return pathsList.size();
    }

    @Override
    public Object getItem(int position) {
        return pathsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.gallery_list_item,null);
        }

        final TextView pathName = convertView.findViewById(R.id.path_name);

        final String elementName = pathsList.get(position).getName();

        pathName.setText(elementName);

        return convertView;
    }

}

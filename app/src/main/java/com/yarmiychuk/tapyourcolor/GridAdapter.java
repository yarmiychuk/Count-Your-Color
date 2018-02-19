package com.yarmiychuk.tapyourcolor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Dmitriy Yarmiychuk on 01.01.18.
 * Создал Dmitriy Yarmiychuk 01.01.18.
 */

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mCellColors = new int[16];
    private int heightOfCell;

    GridAdapter(Context context, int[] cellColors, int heightOfCell) {
        this.mContext = context;
        this.mCellColors = cellColors;
        this.heightOfCell = heightOfCell;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View gridCell = convertView;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                gridCell = inflater.inflate(R.layout.grid_cell, parent, false);
                gridCell.setLayoutParams(new AbsListView.LayoutParams(heightOfCell, heightOfCell));
            }
        }
        // Set background color for cell
        int color = ContextCompat.getColor(mContext, R.color.colorWhite);
        switch (mCellColors[position]) {
            case MainActivity.COLOR_RED:
                color = ContextCompat.getColor(mContext, R.color.colorRed);
                break;
            case MainActivity.COLOR_BLUE:
                color = ContextCompat.getColor(mContext, R.color.colorBlue);
                break;
            case MainActivity.COLOR_GREEN:
                color = ContextCompat.getColor(mContext, R.color.colorGreen);
                break;
            case MainActivity.COLOR_YELLOW:
                color = ContextCompat.getColor(mContext, R.color.colorYellow);
                break;
        }
        TextView textView = gridCell.findViewById(R.id.cell_text_view);
        textView.setBackgroundColor(color);

        return gridCell;
    }

    @Override
    public int getCount() {
        return mCellColors.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}

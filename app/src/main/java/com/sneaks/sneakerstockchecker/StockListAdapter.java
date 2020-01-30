package com.sneaks.sneakerstockchecker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jimmy on 2017-05-14
 */

class StockListItem {
    private String size;
    private String number;

    public String getSize() {
        return size;
    }
    public String getNumber() {
        return number;
    }

    public void setSize (String size) {
        this.size = size;
    }
    public void setNumber (String number) {
        this.number = number;
    }
}

public class StockListAdapter extends BaseAdapter {
    private ArrayList<StockListItem> stockListItem;
    private LayoutInflater layoutInflater;

    public StockListAdapter(Context context, ArrayList<StockListItem> stockListItem) {
        this.stockListItem = stockListItem;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return stockListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return stockListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stock_list_layout, null);
            holder = new ViewHolder();
            holder.stockSize = (TextView) convertView.findViewById(R.id.sizeTextView);
            holder.stockNumber = (TextView) convertView.findViewById(R.id.stockTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String size = stockListItem.get(position).getSize();
        String number = stockListItem.get(position).getNumber();
        holder.stockSize.setText(size);
        holder.stockSize.setTypeface(null, Typeface.NORMAL);
        holder.stockSize.setAllCaps(false);
        holder.stockNumber.setText(number);
        holder.stockNumber.setTypeface(null, Typeface.NORMAL);
        holder.stockNumber.setAllCaps(false);
        return convertView;
    }

    static class ViewHolder {
        TextView stockSize;
        TextView stockNumber;
    }
}


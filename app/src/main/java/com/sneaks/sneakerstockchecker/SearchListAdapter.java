package com.sneaks.sneakerstockchecker;

/**
 * Created by Jimmy on 2017-05-09.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

class SearchListItem {
    private String name;
    private String pid;
    private Bitmap image;

    public String getName() {
        return name;
    }
    public String getPid() {
        return pid;
    }
    public Bitmap getImage() {
        return image;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPid (String pid) {
        this.pid = pid;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
}

public class SearchListAdapter extends BaseAdapter {
    private ArrayList<SearchListItem> searchListItem;
    private LayoutInflater layoutInflater;
    private String stockUrlStart;
    private Context context;

    public SearchListAdapter(Context context, ArrayList<SearchListItem> searchListItem, String stockUrlStart) {
        this.searchListItem = searchListItem;
        this.stockUrlStart = stockUrlStart;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return searchListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return searchListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.search_list_layout, null);
            holder = new ViewHolder();
            holder.productName = (TextView) convertView.findViewById(R.id.productNameTextView);
            holder.productPid = (TextView) convertView.findViewById(R.id.productIdTextView);
            holder.productImage = (ImageView) convertView.findViewById(R.id.productImageView);
            holder.viewStockButton = (Button) convertView.findViewById(R.id.viewStockButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String name = searchListItem.get(position).getName();
        String pid = searchListItem.get(position).getPid();
        holder.productName.setText(name);
        holder.productName.setAllCaps(false);
        holder.productPid.setText(pid);
        holder.productPid.setAllCaps(false);
        holder.productImage.setImageBitmap(searchListItem.get(position).getImage());
        View.OnClickListener myButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int position = listView.getPositionForView(parentRow);
                Log.i("asd", position + "");
                Intent intent = new Intent(context, StockSearchService.class);
                intent.putExtra("parm1", "SEARCH_STOCK");
                intent.putExtra("parm2", stockUrlStart);
                intent.putExtra("parm3", searchListItem.get(position).getPid().toUpperCase());
                context.startService(intent);

            }
        };
        holder.viewStockButton.setOnClickListener(myButtonClickListener);
        return convertView;
    }

    static class ViewHolder {
        TextView productName;
        TextView productPid;
        ImageView productImage;
        Button viewStockButton;
    }
}

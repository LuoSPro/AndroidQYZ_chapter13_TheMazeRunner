package com.example.themazerunner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.themazerunner.util.ScreenUtil;

import java.util.List;

public class GridItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<Bitmap> mBitmapList;


    public GridItemAdapter(Context context, List<Bitmap> bitmapList) {
        mContext = context;
        mBitmapList = bitmapList;
    }

    public void setBitmapList(List<Bitmap> bitmapList) {
        mBitmapList = bitmapList;
    }

    @Override
    public int getCount() {
        return mBitmapList.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return mBitmapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView itemImg = null;
        int density = (int) ScreenUtil.getDeviceDensity(mContext);
        if (convertView == null){
            itemImg = new ImageView(mContext);
            //设置布局图片
            itemImg.setLayoutParams(new GridView.LayoutParams(120*density,140*density));
            //设置显示比例类型
            itemImg.setScaleType(ImageView.ScaleType.FIT_XY);
        }else {
            itemImg = (ImageView) convertView;
        }
        itemImg.setBackgroundColor(Color.BLUE);
        itemImg.setImageBitmap(mBitmapList.get(position));
        return itemImg;
    }
}

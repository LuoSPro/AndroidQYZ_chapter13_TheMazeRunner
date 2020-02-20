package com.example.themazerunner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.themazerunner.R;
import com.example.themazerunner.activity.PuzzleMain;
import com.example.themazerunner.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像工具类，实现图像的分割与自适应
 */
public class ImagesUtil {

    public ItemBean mItemBean;


    /**
     * 切图、初始状态(正常顺序)
     *
     * @param type 游戏种类
     * @param picSelected 选择的图片
     * @param context context
     */
    public void createInitBitmap(int type, Bitmap picSelected, Context context){
        Bitmap bitmap = null;
        List<Bitmap> bitmapItems = new ArrayList<>();
        //每个Item的宽高
        int itemWidth = picSelected.getWidth()/type;
        int itemHeight = picSelected.getHeight()/type;
        for (int i = 1; i <= type ; i++) {
            for (int j = 1; j <= type; j++) {
                bitmap = Bitmap.createBitmap(
                        picSelected,
                        (j-1)*itemWidth,
                        (i-1)*itemHeight,
                        itemWidth,
                        itemHeight);
                bitmapItems.add(bitmap);
                mItemBean = new ItemBean(
                        (i-1)*type + j,
                        (i-1)*type + j,
                        bitmap);
                //将此处分割的一个一个的图片封装成ItemBean，并保存到GameUtil类中，以供使用
                GameUtil.mItemBeans.add(mItemBean);
            }
        }
        //保存最后一个图片在拼图完成时填充
        PuzzleMain.mLastBitmap = bitmapItems.get(type*type-1);
        //设置最后一个为空Item
        bitmapItems.remove(type*type-1);
        GameUtil.mItemBeans.remove(type*type-1);
        Bitmap blankBitmap = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.blank);
        blankBitmap = Bitmap.createBitmap(blankBitmap,0,0,itemWidth,itemHeight);
        bitmapItems.add(blankBitmap);
        GameUtil.mItemBeans.add(new ItemBean(type*type,0,blankBitmap));
        GameUtil.mBlankItemBean = GameUtil.mItemBeans.get(type*type-1);
    }


    /**
     * 处理图片 放大、缩小到合适位置
     *
     * @param newWidth 缩放后的Width
     * @param newHeight 缩放后的Height
     * @param bitmap bitmap
     * @return
     */
    public Bitmap resizeBitmap(float newWidth,float newHeight,Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postScale(newWidth/bitmap.getWidth(),newHeight/bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(
                bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),
                matrix,true);
        return newBitmap;
    }
}

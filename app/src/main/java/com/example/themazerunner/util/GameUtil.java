package com.example.themazerunner.util;

import com.example.themazerunner.activity.PuzzleMain;
import com.example.themazerunner.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

public class GameUtil {
    public static List<ItemBean> mItemBeans = new ArrayList<>();
    public static ItemBean mBlankItemBean;


    /**
     * 生成随机的Item
     */
    public static void getPuzzleGenerator(){
        int index = 0;
        //随机打乱顺序
        for (int i = 0; i < mItemBeans.size(); i++) {
            index = (int) (Math.random()* PuzzleMain.TYPE*PuzzleMain.TYPE);
            swapItems(mItemBeans.get(index),GameUtil.mBlankItemBean);
        }
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < mItemBeans.size(); i++) {
            data.add(mItemBeans.get(i).getBitmapId());
        }
        //判断生成是否有解
        if (canSolve(data)){//有解，则生成随机Item成功
            return;
        }else {//否则，递归调用自己，直到生成的随机Item有解时
            getPuzzleGenerator();
        }
    }

    /**
     * 交换空格和点击的Item的位置
     *
     * @param from 交换图
     * @param blank 空白图
     */
    public static void swapItems(ItemBean from,ItemBean blank){
        ItemBean tempItemBean = new ItemBean();
        //交换BitmapId
        tempItemBean.setBitmapId(from.getBitmapId());
        from.setBitmapId(blank.getBitmapId());
        blank.setBitmapId(tempItemBean.getBitmapId());
        //交换Bitmap
        tempItemBean.setBitmap(from.getBitmap());
        from.setBitmap(blank.getBitmap());
        blank.setBitmap(tempItemBean.getBitmap());
        //设置新的Blank
        GameUtil.mBlankItemBean = from;
    }

    /**
     * 该数据是否有解
     *
     * @param data 拼图数组数据
     * @return 该数据是否有解
     */
    public static boolean canSolve(List<Integer> data){
        //获取空格ID
        int blankId = GameUtil.mBlankItemBean.getBitmapId();
        //可行性原则，先判断是否满足基本条件，再运用算法判断是否有解
        if (data.size()%2 == 1){
            return getInversions(data)%2 == 0;//通过计算倒置和算法判断是否有解
        }else {
            //从下往上数，空格位于奇数行
            if (((blankId-1)/PuzzleMain.TYPE)%2 == 1){
                return getInversions(data)%2 == 0;
            }else {
                //从下往上数。空格位于奇数行
                return getInversions(data)%2 == 1;
            }
        }
    }

    /**
     * 计算倒置和算法
     *
     * @param data 拼图数组数据
     * @return 该序列的倒置和
     */
    public static int getInversions(List<Integer> data){
        int inversions = 0;
        int inversionCount = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.size(); j++) {
                int index = data.get(i);
                if (data.get(i) != 0&&data.get(i) < index){
                    inversionCount++;
                }
            }
            inversions += inversionCount;
            inversionCount = 0;
        }
        return inversions;
    }


    /**
     * 判断点击的Item是否可移动
     *
     * @param position position
     * @return 能否移动
     */
    public static boolean isMoveable(int position){
        int type = PuzzleMain.TYPE;
        //获取空格Item
        int blankId = GameUtil.mBlankItemBean.getItemId() - 1;
        //不同行相差为type
        if (Math.abs(blankId - position) == type){
            return true;
        }
        //相同行，相差1
        if ((blankId/type == position/type)&&Math.abs(blankId-position) == 1){
            return true;
        }
        return false;
    }


    /**
     * 是否拼图成功
     *
     * @return
     */
    public static boolean isSuccess(){
        for(ItemBean tempBean:GameUtil.mItemBeans){
            if (tempBean.getBitmapId() != 0&&(tempBean.getItemId()) == tempBean.getBitmapId() ){
                continue;
            }else if (tempBean.getBitmapId() == 0&&tempBean.getItemId() == PuzzleMain.TYPE*PuzzleMain.TYPE){
                continue;
            }else {
                return false;
            }
        }
        return true;
    }
}

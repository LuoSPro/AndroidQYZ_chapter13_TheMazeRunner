package com.example.themazerunner.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.themazerunner.R;
import com.example.themazerunner.adapter.GridItemAdapter;
import com.example.themazerunner.adapter.GridPicListAdapter;
import com.example.themazerunner.bean.ItemBean;
import com.example.themazerunner.util.GameUtil;
import com.example.themazerunner.util.ImagesUtil;
import com.example.themazerunner.util.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PuzzleMain extends AppCompatActivity implements View.OnClickListener{

    //最后的空白
    public static Bitmap mLastBitmap;
    //游戏类型(难度)
    public static int TYPE;
    //计时器
    private Timer mTimer;
    private TimerTask mTimerTask;
    //查看原图时弹出的image(动态加载)
    private ImageView mImageView;
    //从外面选择进来的照片
    private Bitmap mPicSelected;
    //判断image是否该显示，受原图按钮控制
    private boolean mIsShowImg = false;
    //显示步数
    private TextView mTvPuzzleMainCounts;
    //记录时间
    private TextView mTvPuzzleMainTimer;
    //记录步数
    private static int COUNT_INDEX = 0;
    //记录时间
    private static int TIME_INDEX = 0;
    //GridView的适配器
    private GridItemAdapter mAdapter;
    //拼图视图
    private GridView mGvPuzzleMainDetail;
    //保存分割后的图片
    private List<Bitmap> picList;
    //工具类
    private ImagesUtil mImagesUtil;
    // Button
    private Button btnBack;
    private Button btnImage;
    private Button btnRestart;

    private static final String TAG = "PuzzleMain";

    /**
     * UI更新Handler
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 更新计时器
                    TIME_INDEX++;
                    mTvPuzzleMainTimer.setText("" + TIME_INDEX);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_main);

        // 初始化Views
        initData();
        // 对图片处理
        handlerImage(mPicSelected);
        // 生成游戏数据
        generateGame();
        // GridView点击事件
        mGvPuzzleMainDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                // 判断是否可移动
                if (GameUtil.isMoveable(position)) {
                    // 交换点击Item与空格的位置
                    GameUtil.swapItems(GameUtil.mItemBeans.get(position), GameUtil.mBlankItemBean);
                    // 重新获取图片
                    recreateData();
                    // 通知GridView更改UI
                    mAdapter.notifyDataSetChanged();
                    // 更新步数
                    COUNT_INDEX++;
                    mTvPuzzleMainCounts.setText("" + COUNT_INDEX);
                    // 判断是否成功
                    if (GameUtil.isSuccess()) {
                        // 将最后一张图显示完整
                        recreateData();
                        picList.remove(TYPE * TYPE - 1);
                        picList.add(mLastBitmap);
                        // 通知GridView更改UI
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(PuzzleMain.this, "拼图成功!", Toast.LENGTH_LONG).show();
                        mGvPuzzleMainDetail.setEnabled(false);
                        mTimer.cancel();
                        mTimerTask.cancel();
                    }
                }
            }
        });
        // 返回按钮点击事件
        btnBack.setOnClickListener(this);
        // 显示原图按钮点击事件
        btnImage.setOnClickListener(this);
        // 重置按钮点击事件
        btnRestart.setOnClickListener(this);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        //从MainActivity传过来的数据：
        TYPE = Objects.requireNonNull(getIntent().getIntExtra("mType",2));
        int bitmapId = Objects.requireNonNull(getIntent().getIntExtra("picSelectedID",-1));
        if (bitmapId == -1){
            String path = getIntent().getStringExtra("mPicPath");
            mPicSelected = BitmapFactory.decodeFile(path);
        }else {
            mPicSelected = BitmapFactory.decodeResource(getResources(), bitmapId);
        }
        //此活动下的数据：
        mLastBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.blank);
        mImagesUtil = new ImagesUtil();
        picList = new ArrayList<>();
        //绑定控件
        mTvPuzzleMainCounts = findViewById(R.id.tv_puzzle_main_counts);
        mGvPuzzleMainDetail = findViewById(R.id.gv_puzzle_main_detail);
        mTvPuzzleMainTimer = findViewById(R.id.tv_puzzle_main_time);
        // Button
        btnBack = findViewById(R.id.btn_puzzle_main_back);
        btnImage = findViewById(R.id.btn_puzzle_main_img);
        btnRestart = findViewById(R.id.btn_puzzle_main_restart);
        //设置为N*N显示
        mGvPuzzleMainDetail.setNumColumns(TYPE);
        RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(
                mPicSelected.getWidth(),mPicSelected.getHeight());
        //水平居中
        gridParams.addRule(RelativeLayout.CENTER_VERTICAL);
        //其他格式熟悉
        gridParams.addRule(RelativeLayout.BELOW,R.id.ll_puzzle_main_spinner);
        gridParams.addRule(RelativeLayout.ABOVE,R.id.ll_puzzle_main_btns);
        //Grid显示
        mGvPuzzleMainDetail.setLayoutParams(gridParams);
        mGvPuzzleMainDetail.setHorizontalSpacing(0);
        mGvPuzzleMainDetail.setVerticalSpacing(0);

        //TextView步数
        mTvPuzzleMainCounts.setText(""+COUNT_INDEX);
        //TextView计时器
        mTvPuzzleMainTimer.setText("0秒");

        //添加显示原图的view
        addImgView();
    }

    /**
     * 添加显示原图的View
     */
    private void addImgView(){
        RelativeLayout relativeLayout = findViewById(R.id.rl_puzzle_main_layout);
        mImageView = new ImageView(PuzzleMain.this);
        mImageView.setImageBitmap(mPicSelected);
        int x = (int) (mPicSelected.getWidth()*0.9F);
        int y = (int) (mPicSelected.getHeight()*0.9F);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x,y);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImageView.setLayoutParams(params);
        relativeLayout.addView(mImageView);
        mImageView.setVisibility(View.GONE);
    }

    /**
     * 对图片处理 自适应大小
     *
     * @param bitmap
     */
    private void handlerImage(Bitmap bitmap) {
        // 将图片放大到固定尺寸
        int screenWidth = ScreenUtil.getScreenSize(this).widthPixels;
        int screenHeight = ScreenUtil.getScreenSize(this).heightPixels;
         //处理图片，调整图片，使其适配游戏界面
        mPicSelected = new ImagesUtil().resizeBitmap(screenWidth * 0.8f, screenHeight * 0.6f, bitmap);
    }


    /**
     * Button点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        switch (v.getId()){
            case R.id.btn_puzzle_main_back:
                PuzzleMain.this.finish();
                break;
            case R.id.btn_puzzle_main_img:
                Animation animShow = AnimationUtils.loadAnimation(
                        PuzzleMain.this,R.anim.image_show_anim);
                Animation animHide = AnimationUtils.loadAnimation(
                        PuzzleMain.this,R.anim.image_hide_anim);
                if (mIsShowImg){
                    mImageView.startAnimation(animHide);
                    mImageView.setVisibility(View.GONE);
                    mIsShowImg = false;
                }else {
                    mImageView.startAnimation(animShow);
                    mImageView.setVisibility(View.VISIBLE);
                    mIsShowImg = true;
                }
                break;
            case R.id.btn_puzzle_main_restart:
                cleanConfig();
                generateGame();
                recreateData();
                //通知GridView更名UI
                mTvPuzzleMainCounts.setText(""+COUNT_INDEX);
                mAdapter.notifyDataSetChanged();
                mGvPuzzleMainDetail.setEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * 返回时调用
     */
    @Override
    protected void onStop() {
        super.onStop();
        // 清空相关参数设置
        cleanConfig();
        this.finish();
    }



    /**
     * 重置时间和步数
     */
    private void cleanConfig(){
        // 清空相关参数设置
        GameUtil.mItemBeans.clear();
        // 停止计时器
        mTimer.cancel();
        mTimerTask.cancel();
        COUNT_INDEX = 0;
        TIME_INDEX = 0;
        // 清除拍摄的照片
//        if (picPath != null) {
//            // 删除照片
//            File file = new File(MainActivity.TEMP_IMAGE_PATH);
//            if (file.exists()) {
//                file.delete();
//            }
//        }

    }

    /**
     * 重新生成游戏界面
     */
    private void generateGame(){


//        mPicSelected = mImagesUtil.resizeBitmap(ScreenUtil.getScreenSize(PuzzleMain.this).widthPixels,
//                1000,mPicSelected);
        //将图片传到ImageUtil中进行分割
        mImagesUtil.createInitBitmap(TYPE,mPicSelected,PuzzleMain.this);
        //将分割的有序的图片进行顺序打乱，并使其有解
        GameUtil.getPuzzleGenerator();
        //将处理过的图片放到GridView上
        for (int i = 0; i < GameUtil.mItemBeans.size(); i++) {
            picList.add(GameUtil.mItemBeans.get(i).getBitmap());
        }
        mAdapter = new GridItemAdapter(PuzzleMain.this,picList);
        mGvPuzzleMainDetail.setAdapter(mAdapter);

        //启动计时器
        mTimer = new Timer(true);
        //计时器线程
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };
        //每1000ms执行 延迟0s
        mTimer.schedule(mTimerTask,0,1000);
    }

    /**
     * 重新设置数据
     */
    private void recreateData(){
        picList.clear();
        for (ItemBean temp : GameUtil.mItemBeans) {
            picList.add(temp.getBitmap());
        }
    }


}

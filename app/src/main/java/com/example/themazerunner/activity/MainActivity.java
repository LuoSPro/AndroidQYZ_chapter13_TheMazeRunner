package com.example.themazerunner.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import com.example.themazerunner.R;
import com.example.themazerunner.adapter.GridPicListAdapter;
import com.example.themazerunner.util.ScreenUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //弹出窗口，但是我用Dialog代替了
    private PopupWindow mPopupWindow;
    private View mPopupView;
    //展示原图片的GridView
    private GridView mGvPicList;
    //保存原图片的id
    private int[] mResPicId;
    //保存原图片
    private List<Bitmap> mPicList;

    //保存难度系数2x2,3x3,4x4
    private int mType = 3;

    //相册
    private static final int CHOOSE_PHOTO = 100;
    //拍照
    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    //用于保存拍照图片的uri
    private Uri mCameraUri;
    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    //如果不加static，当跳转之后再回来，这个变量就为null了,但不是长久之计
    private static String mCameraImagePath;
    // 是否是Android 10以上手机
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private static final int CAMERA_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPicList = new ArrayList<>();
        mGvPicList = findViewById(R.id.gv_puzzle_main_pic_list);
        //初始化Bitmap
        mResPicId = new int[]{
                R.drawable.pic1,
                R.drawable.pic2,
//                R.drawable.pic3,
//                R.drawable.pic4,
                R.drawable.pic5,
                R.drawable.pic6,
                R.drawable.pic7,
//                R.drawable.pic8,
//                R.drawable.pic9,
//                R.drawable.pic10,
                R.drawable.pic11,
                R.drawable.ic_launcher_foreground
        };
        Bitmap[] bitmaps = new Bitmap[mResPicId.length];
        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(),mResPicId[i]);
            mPicList.add(bitmaps[i]);
        }
        mGvPicList.setAdapter(new GridPicListAdapter(MainActivity.this,mPicList));

        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mPicList.size()-1){
                    showBottomDialog();
                }else {
                    //选择当前展示得图片
                    Intent intent = new Intent(MainActivity.this,PuzzleMain.class);
                    intent.putExtra("picSelectedID",mResPicId[position]);
                    intent.putExtra("mType",mType);
                    startActivity(intent);
                }
            }

        });
    }



    /**
     * 用来显示 popup window
     *`
     * @param view
     */
    private void popupShow(View view){
        int density = (int) ScreenUtil.getDeviceDensity(this);
        mPopupWindow = new PopupWindow(mPopupView,200*density,50*density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        //透明背景
        Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(transparent);
        //获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
                location[0]-40*density,location[1]+30*density);
    }


    //显示Dialog
    private void showBottomDialog(){
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(this,R.style.DialogTheme);
        //2、设置布局
        View view = View.inflate(this,R.layout.my_account_dialog_layout,null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.myAccount_takePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionAndCamera();
                dialog.dismiss();
            }
        });

        //相册
        dialog.findViewById(R.id.myAccount_takePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //权限检查
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });

        dialog.findViewById(R.id.myAccount_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    //从相册中选择照片
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    /**
     * 检查权限并拍照。
     * 调用相机前先检查权限。
     */
    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            openCamera();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this,"拍照权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(MainActivity.this,PuzzleMain.class);
        intent.putExtra("mType",mType);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                if (isAndroidQ) {
//                    // Android 10 使用图片uri加载
//                    ivPhoto.setImageURI(mCameraUri);
//                } else {
//                    // 使用图片路径加载
//                    ivPhoto.setImageBitmap(BitmapFactory.decodeFile(mCameraImagePath));
//                }
                intent.putExtra("mPicPath",mCameraImagePath);
                startActivity(intent);
            } else {
                Toast.makeText(this,"取消",Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == CHOOSE_PHOTO){
            if (requestCode != RESULT_OK) {
                String path;
                //判断手机的版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    //4.4及以上的系统使用的这个方法处理图片
                    path = handleImageOnKitKat(data);
                }else {
                    //4.4以下的系统使用的图片处理方法
                    path = handleImageBeforeKitKat(data);
                }
                intent.putExtra("mPicPath", path);
                startActivity(intent);
            }
        }
    }

    private String handleImageOnKitKat(Intent data) {

        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)) {
            //如果document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://" +
                        "downloads//public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果是普通content类型的uri，则使用普通的方法处理
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果使用file类型的uri，直接获取图片的路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        return imagePath;
    }

    private String getImagePath(Uri externalContentUri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(externalContentUri,
                null,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            }
            cursor.close();
        }
        return path;
    }
}

package tw.org.iii.travelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.githang.statusbar.StatusBarCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class SettingActivity extends AppCompatActivity {
    final int REQUEST_TAKE_PHOTO = 1;
    final int IMAGE_REQUEST_CODE = 2;
    final int SELECT_PIC_NOUGAT = 3;
    private ListView setting_list;
    private PopupWindow popupWindow;
    private Button btnConfirm;
    private String [] data1, data2;
    private MyAdapter myAdapter;
    private LinearLayout iv_home, iv_guide, iv_camera, iv_favorite, backgroundColor;
    private CircleImageView circleImageView;
    private ImageView takePhoto;
    private Uri photoURI, uriForFile;
    private File photoFile, storageDir, mGalleryFile, stickerFile;
    private String mCurrentPhotoPath, sticker, realPath, drawableImageUri;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String path = Environment.getExternalStorageDirectory() +
            "/Android/data/tw.org.iii.travelapp/files/Pictures";
    private boolean isOriginal = false;
    private boolean isSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        sp = getSharedPreferences("memberdata", MODE_PRIVATE);
        editor = sp.edit();
        findView();
        init();

        setIconListener();
    }
    //解決intent更換大頭照後並不會跑onCreate
    @Override
    protected void onResume() {
        super.onResume();
        //設定背景顏色
        setBackgroundColor();
        isSignIn = sp.getBoolean("signin", false);
        sticker = sp.getString("sticker", null);
        if(sticker != null){
            stickerFile = new File(sticker);
            Bitmap bitmap = BitmapFactory.decodeFile(stickerFile.getAbsolutePath());
            circleImageView.setImageBitmap(bitmap);
            isOriginal = false;
        }else {
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.sticker);
            circleImageView.setImageBitmap(bitmap);
            isOriginal = true;
        }
        boolean isLogin = sp.getBoolean("signin", false);
        if(isLogin){
            data1[0] = "登出";
        }else{
            data1[0] = "登入";
        }
        myAdapter.notifyDataSetChanged();
        Log.v("brad", "onResume isLogin = "+ isLogin);
    }


    private void init(){
        data1 = new String[]{"登出", "個人資料", "背景顏色", "相片集", "關於我"};


        data2 = new String[]{};
        myAdapter = new MyAdapter(SettingActivity.this);
        setting_list.setAdapter(myAdapter);
        setting_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    //登入&登出
                    case 0:
                        Intent loginIntent = new Intent(
                                SettingActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        break;
                    //個人資料
                    case 1:
                        gotoProfile();
                        break;
                    //更換背景顏色
                    case 2:
                        gotoTheme();
                        break;
                    //我的相片集
                    case 3:
                        String url = new MyApplication().url +
                                "/fsit04/appimg?user_id=" +
                                new MyApplication().user_id;
                        Intent galleryIntent = new Intent(
                                SettingActivity.this, PhotoAlbumActivity.class);
                        galleryIntent.putExtra("url", url);
                        startActivity(galleryIntent);
                        break;
                    //關於我
                    case 4:
                        gotoAboutMe();
                        break;
                }
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOriginal) {
                    Intent intent = new Intent(SettingActivity.this, ShowStickerActivity.class);
                    intent.putExtra("isOriginal", true);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(SettingActivity.this, ShowStickerActivity.class);
                    intent.putExtra("isOriginal", false);
                    startActivity(intent);
                }
//                Intent intent = new Intent();
//                //開啟Pictures畫面Type設定為image
//                intent.setType("image/*");
//                //使用Intent.ACTION_GET_CONTENT這個Action
//                //會開啟選取圖檔視窗讓您選取手機內圖檔
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                //取得相片後返回本畫面
//                startActivityForResult(intent, 0);
            }
        });
        //更換使用者頭像
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] data = {"相機", "相簿"};
                final NormalListDialog dialog = new NormalListDialog(SettingActivity.this, data);
                dialog.title("請選擇")
                        .titleTextSize_SP(20)
                        .itemTextSize(20)
                        .setOnOperItemClickL(new OnOperItemClickL() {
                            @Override
                            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position){
                                    //從相機
                                    case 0:
                                        gotoTakePhoto();
                                        dialog.dismiss();
                                        break;
                                    //從相簿
                                    case 1:
                                        gotoAlbum();
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        });
                dialog.show();
            }
        });
    }
    //找出view
    private void findView(){
        setting_list = findViewById(R.id.setting_list);
        iv_home = findViewById(R.id.btn_home);
        iv_guide = findViewById(R.id.btn_guide);
        iv_camera = findViewById(R.id.btn_camera);
        iv_favorite = findViewById(R.id.btn_favorite);
        circleImageView = findViewById(R.id.setting_photo);
        takePhoto = findViewById(R.id.setting_takePhoto);
        backgroundColor = findViewById(R.id.setting_background);
    }

    private void setBackgroundColor(){
        String backGroundColor = sp.getString("backgroundColor", "#FFFFDD");
        backgroundColor.setBackgroundColor(Color.parseColor(backGroundColor));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                //拍照回來
                case REQUEST_TAKE_PHOTO:
                    Uri dataUri = FileProvider.getUriForFile
                            (this, "tw.org.iii.travelapp", photoFile);
                    GlideApp
                            .with(this)
                            .load(dataUri)
                            .circleCrop()
                            .into(circleImageView);
                    editor.putString("sticker", photoFile.getAbsolutePath());
                    editor.commit();
                    break;
                //從相簿選擇照片回來-Nougat之前的版本
                case IMAGE_REQUEST_CODE:
                    Uri uri1 = data.getData();
                    GlideApp
                            .with(this)
                            .load(uri1)
                            .circleCrop()
                            .into(circleImageView);
                    realPath = getPathFromUri(SettingActivity.this,uri1);
                    editor.putString("sticker", realPath);
                    editor.commit();
                    break;
                //從相簿選擇照片回來-Nougat之後的版本
                case SELECT_PIC_NOUGAT:
                    Uri uri2 = data.getData();
                    GlideApp
                            .with(this)
                            .load(uri2)
                            .circleCrop()
                            .into(circleImageView);
                    realPath = getPathFromUri(SettingActivity.this,uri2);
                    editor.putString("sticker", realPath);
                    editor.commit();
                    break;
            }
        }
    }
    //設定按鈕事件
    private void setIconListener(){
        //首頁
        iv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //導覽
        iv_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    Intent intent = new Intent(SettingActivity.this, MapsActivity.class);
                    startActivity(intent);
                }else {
                    Intent loginIntent = new Intent(
                            SettingActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        });
        //相機
        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gotoTakePhoto();
            }
        });
        //我的最愛
        iv_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    Intent intent = new Intent(SettingActivity.this, FavoriteActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent loginIntent = new Intent(SettingActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        });
    }
    //個人資料選單
    private void gotoProfile(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    //更換背景顏色
    private void gotoTheme(){
        Intent intent = new Intent(this, ThemesActivity.class);
        startActivity(intent);
    }
    //關於我選單
    private void gotoAboutMe() {
        final PrettyDialog dialog = new PrettyDialog(this);
            dialog.setTitle("開發人員名單")
                .setIcon(
                    R.drawable.pdlg_icon_info,     // icon resource
                    R.color.pdlg_color_green,      // icon tint
                    new PrettyDialogCallback() {   // icon OnClick listener
                        @Override
                        public void onClick() {
                            // Do what you gotta do
                        }
                    })
                .setMessage("劉昭延" + "\n" +
                            "粘子詮" + "\n" +
                            "倪偉城" + "\n" +
                            "蔡篤賢" + "\n")
                .addButton(
                        "確認",
                        R.color.pdlg_color_white,
                        R.color.pdlg_color_green,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                dialog.dismiss();
                            }
                        }
                ).show();

//        View view = LayoutInflater.from(this)
//                //設定輸出的layout
//                .inflate(R.layout.layout_about_me, null);
//        popupWindow = new PopupWindow(view);
//        //設定popupWindow的寬、高
//        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    popupWindow.dismiss();
//                }
//                return true;
//            }
//        });
//
//        btnConfirm =  view.findViewById(R.id.confirm);
//        btnConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                popupWindow.dismiss();
//            }
//        });
//        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, 0);

    }
    //BaseAdapter for ListView
    private class MyAdapter extends BaseAdapter{

        Context myContext;
        LayoutInflater inflater;

        public MyAdapter(Context context){
            this.myContext = context;
            inflater = LayoutInflater.from(this.myContext);
        }
        //取得list的數量
        @Override
        public int getCount() {
            return data1.length;
        }

        @Override
        public Object getItem(int position) {
            return data1[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //設置ListView的layout,沒有根目錄root值為null
            convertView = inflater.inflate(R.layout.layout_setting, null);
            //取得ListView layout的每個view
            TextView item_title = convertView.findViewById(R.id.item_title);
            //給值
            item_title.setText(data1[position]);
            //回傳convertView
            return convertView;
        }
    }
    //使用相機拍照更換個人頭像
    private void gotoTakePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 確保有相機來處理intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
                Log.v("brad", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                        SettingActivity.this,
                        "tw.org.iii.travelapp",
                        photoFile);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(Intent.createChooser(takePictureIntent, "TakePhoto"), REQUEST_TAKE_PHOTO);
            }
        }
    }
    //取得日期格式的照片名稱
    private File createImageFile() throws IOException {
        // Create an image file name
        // timeStamp的格式-> 20180420_004839
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /**  getExternalFilesDir() 需要給的是type參數
         *   傳回的是該app packagename 底下,參數的系統位置
         *  storage/emulated/0/Android/data/tw.org.iii.takepicturetest/files/Pictures
         */
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    //使用相簿照片更換個人頭像
    private void gotoAlbum(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mGalleryFile = new File(path, timeStamp + ".jpg");//相册的File对象
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果大于等于7.0使用FileProvider
            uriForFile = FileProvider.getUriForFile
                    (this, "tw.org.iii.travelapp", mGalleryFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, SELECT_PIC_NOUGAT);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGalleryFile));
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
        }
    }
    //使用uri取得照片實際存放位置
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

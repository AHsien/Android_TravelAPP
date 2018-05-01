package tw.org.iii.travelapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bm.library.PhotoView;
import com.githang.statusbar.StatusBarCompat;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SharePhotoActivity extends AppCompatActivity {
    private ImageView iv_TakePicture, iv_Album, iv_Upload;
    private PhotoView photoView;
    private File photoFile;
    private Uri dataUri, photoURI, uriForFile;
    private Bitmap bitmap;
    private RequestQueue queue;
    private String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 0;
    static final int SELECT_PIC_NOUGAT = 1;
    static final int IMAGE_REQUEST_CODE = 2;
    private String path = Environment.getExternalStorageDirectory()+"/Android/data/tw.org.iii.travelapp/files/Pictures";
    private File mGalleryFile;
    private int statusCode;
    private MyHandler myHandler;
    private ProgressWheel progressWheel;
    private Toolbar toolbar;
    private FrameLayout backgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_photo);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        findView();
        myHandler = new MyHandler();
        queue= Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("memberdata", MODE_PRIVATE);
        String backGroundColor = sp.getString("backgroundColor", "#FFFFDD");
        backgroundColor.setBackgroundColor(Color.parseColor(backGroundColor));
    }

    private void findView(){
        photoView = findViewById(R.id.sharePhoto_PhotoView);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.share_photo_menu);
        setSupportActionBar(toolbar);
        progressWheel = findViewById(R.id.progress_wheel);
        backgroundColor =findViewById(R.id.sharePhoto_background);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.share_photo_menu,menu);
        MenuItem takePictureItem = menu.findItem(R.id.TakePicture);
        MenuItem albumItem = menu.findItem(R.id.Album);
        MenuItem uploadFileItem = menu.findItem(R.id.UploadPhoto);

        //拍照
        takePictureItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                dispatchTakePictureIntent();
                return false;
            }
        });
        //從相簿選擇照片
        albumItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                gotoAlbum();
                return false;
            }
        });
        //上傳照片
        uploadFileItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(bitmap != null) {
                    progressWheel.spin();
                    uploadFile();
                }else{
                    Toast.makeText(SharePhotoActivity.this,
                            "請選擇檔案上傳", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            bitmap = null;
            photoView.enable();
            switch (requestCode){
                //拍照回來
                case REQUEST_TAKE_PHOTO:
                    dataUri = FileProvider.getUriForFile
                            (this, "tw.org.iii.travelapp", photoFile);
                    try {
                        bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(dataUri));
                        photoView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                //從相簿選擇照片回來-Nougat之後的版本
                case SELECT_PIC_NOUGAT:
                    Uri uri = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(
                              getContentResolver().openInputStream(uri));
//                        bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        photoView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    photoView.setImageBitmap(bitmap);
                    break;
                //從相簿選擇照片回來-Nougat之前的版本
                case IMAGE_REQUEST_CODE:
                    dataUri = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(dataUri));
//                        bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        photoView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
    //使用相機拍照取得照片
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        // 確保有相機來處理intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(SharePhotoActivity.this,
                        "tw.org.iii.travelapp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(takePictureIntent, "TakePhoto"), REQUEST_TAKE_PHOTO);
                galleryAddPic();
            }
        }
    }
    //至相簿取得照片
    private void gotoAlbum(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mGalleryFile = new File(path, timeStamp + ".jpg");
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
    //建立相片名稱
    private File createImageFile() throws IOException {
        // Create an image file name
        // timeStamp的格式-> 20180420_004839
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /**  getExternalFilesDir() 需要給的是type參數
         *   傳回的是該app packagename 底下,參數的系統位置
         *  storage/emulated/0/Android/data/tw.org.iii.takepicturetest/files/Pictures
         */
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.v("brad", "image = " + image.getAbsolutePath());
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    /**
     * 將照片新增到系統媒體提供商，讓您的照片易於訪問
     * 如果您將照片保存到提供的目錄中 getExternalFilesDir()，
     * 則媒體掃描程序無法訪問這些文件，因為它們對您的應用程序是私人的。
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    /**
     * 上傳檔案用
     * / http://36.235.38.228:8080/fsit04/photo?user_id=1  傳完到這邊看有沒有成功
     */

    private void uploadFile() {
        String uploadUrl = new MyApplication().url + "/fsit04/saveFile";
        final byte[] data ;
        //路徑上傳
//        File upload =new File(sdroot,"檔案的路徑");
//        data =filePathToByte(upload);

        //BitMap上傳
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.djokovic);
        data=bitmapToBytes(bitmap);

        VolleyMultipartRequest multipartRequest =
                new VolleyMultipartRequest(
                        Request.Method.POST,
                        uploadUrl,
                        new Response.Listener<NetworkResponse>(){
                            @Override
                            public void onResponse(NetworkResponse response) {
                                Log.v("brad", "statusCode=" + response.statusCode);
                                myHandler.sendEmptyMessage(response.statusCode);
                            }
                        },
                        null){
                    @Override
                    protected Map<String, DataPart> getByteData()
                            throws AuthFailureError {

                        HashMap<String,DataPart> params = new HashMap<>();
                        //傳檔案
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        params.put("file",new DataPart(timeStamp + ".jpg", data));
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> m1 =new HashMap<>();
                        //使用者ID
                        m1.put("user_id", new MyApplication().user_id);
                        //景點ID
                        m1.put("total_id","1");
                        //lat
                        m1.put("lat","25.00");
                        //lng
                        m1.put("lng","121.00");
                        return m1;
                    }
                };
        queue.add(multipartRequest);
    }
    /**
     *
     * @param file  檔案路徑轉BYTE 陣列
     * @return
     */
    private byte[] filePathToByte(File file){
        byte[] data = new byte[(int) file.length()];
        try {
            FileInputStream fin = new FileInputStream(file);
            fin.read(data);
            fin.close();
        }catch (Exception e){
            Log.v("brad", e.toString());
        }
        return data;
    }
    /**
     *
     * @param bm    Bitmap 轉Byte[];
     * @return
     */
    private byte[] bitmapToBytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 200){
                Toast.makeText(SharePhotoActivity.this,
                        "上傳成功", Toast.LENGTH_SHORT).show();
                progressWheel.stopSpinning();
            }else{
                Toast.makeText(SharePhotoActivity.this,
                        "上傳失敗", Toast.LENGTH_SHORT).show();
                progressWheel.stopSpinning();
            }
        }
    }
}

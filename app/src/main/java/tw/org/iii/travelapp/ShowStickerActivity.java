package tw.org.iii.travelapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bm.library.PhotoView;
import com.githang.statusbar.StatusBarCompat;

public class ShowStickerActivity extends AppCompatActivity {
    private PhotoView photoView;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean isOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sticker);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        photoView = findViewById(R.id.showSticker_photoView);
        sp = getSharedPreferences("memberdata", MODE_PRIVATE);
        String sticker = sp.getString("sticker", null);

        Intent intent = getIntent();
        isOriginal = intent.getBooleanExtra("isOriginal", true);

        photoView.enable();

        if(isOriginal){
            photoView.setImageResource(R.drawable.sticker);
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(sticker);
            photoView.setImageBitmap(bitmap);
        }
    }
}

package tw.org.iii.travelapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;

import cyd.awesome.material.AwesomeButton;

public class ThemesActivity extends AppCompatActivity {
    private LinearLayout themes_layout;
    private String list[] = {"黃色", "藍色", "綠色", "古白色", "甘露綠", "淡藍色", "淡黃色(系統預設)"};
    private String backgroundColor, colorName;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String[] color;
    private AwesomeButton awesomeButton;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        setTitle("佈景主題更換");
        themes_layout = findViewById(R.id.themes_main);
        awesomeButton = findViewById(R.id.setBackgroundColor);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.profile_menu);
        setSupportActionBar(toolbar);
        getThemeData();

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String backGroundColor = sp.getString("backgroundColor", "#FFFFDD");
        themes_layout.setBackgroundColor(Color.parseColor(backGroundColor));
    }

    private void init(){
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(
                ThemesActivity.this,
                android.R.layout.simple_list_item_single_choice,
                list);

        ListView lv = findViewById(R.id.themes_list);

        lv.setAdapter(adapter);

        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                backgroundColor = (String)parent.getItemAtPosition(position);
                colorName = backgroundColor;
                setThemes();
            }
        });
        //套用按鈕
        awesomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundColor = getBackgroundColor(backgroundColor);
                editor.putString("backgroundColor", backgroundColor);
                editor.commit();
                Toast.makeText(ThemesActivity.this,
                        "背景顏色 (" + colorName + ")已套用",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getThemeData(){
        sp = getSharedPreferences("memberdata", MODE_PRIVATE);
        editor = sp.edit();
        String color = sp.getString("backgroundColor", null);
        //if判斷句用來解決清除app資料時，無法獲得資料而造成的例外
        if(color != null) {
            switch (color) {
                case "#FAEBD7":
                    themes_layout.setBackgroundColor(Color.parseColor("#FAEBD7"));
                    break;
                case "#FFF68F":
                    themes_layout.setBackgroundColor(Color.parseColor("#FFF68F"));
                    break;
                case "#AFEEEE":
                    themes_layout.setBackgroundColor(Color.parseColor("#AFEEEE"));
                    break;
                case "#C1FFC1":
                    themes_layout.setBackgroundColor(Color.parseColor("#C1FFC1"));
                    break;
                case "#F0FFF0":
                    themes_layout.setBackgroundColor(Color.parseColor("#F0FFF0"));
                    break;
                case "#BFEFFF":
                    themes_layout.setBackgroundColor(Color.parseColor("#BFEFFF"));
                    break;
                case "#FFFFDD":
                    themes_layout.setBackgroundColor(Color.parseColor("#FFFFDD"));
                    break;
            }
        }
    }
    //設定背景顏色
    private void setThemes(){
        if(backgroundColor != null) {
            switch (backgroundColor) {
                case "古白色":
                    themes_layout.setBackgroundColor(Color.parseColor("#FAEBD7"));
                    break;
                case "黃色":
                    themes_layout.setBackgroundColor(Color.parseColor("#FFF68F"));
                    break;
                case "藍色":
                    themes_layout.setBackgroundColor(Color.parseColor("#AFEEEE"));
                    break;
                case "綠色":
                    themes_layout.setBackgroundColor(Color.parseColor("#C1FFC1"));
                    break;
                case "甘露綠":
                    themes_layout.setBackgroundColor(Color.parseColor("#F0FFF0"));
                    break;
                case "淡藍色":
                    themes_layout.setBackgroundColor(Color.parseColor("#BFEFFF"));
                    break;
                case "淡黃色(系統預設)":
                    themes_layout.setBackgroundColor(Color.parseColor("#FFFFDD"));
                    break;
            }
        }
    }

    private String getBackgroundColor(String color){
        switch (color){
            case "古白色":
                backgroundColor = "#FAEBD7";
                break;
            case "黃色":
                backgroundColor = "#FFF68F";
                break;
            case "藍色":
                backgroundColor = "#AFEEEE";
                break;
            case "綠色":
                backgroundColor = "#C1FFC1";
                break;
            case "甘露綠":
                backgroundColor = "#F0FFF0";
                break;
            case "淡藍色":
                backgroundColor = "#BFEFFF";
                break;
            case "淡黃色(系統預設)":
                backgroundColor = "#FFFFDD";
                break;
        }
        return backgroundColor;
    }
}
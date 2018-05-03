package tw.org.iii.travelapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cyd.awesome.material.AwesomeButton;

/**
 * Created by User on 2018/4/11.
 */

public class MyDragView extends LinearLayout{
    private MyListView listView;
    private View myView;
    private ArrayList<HashMap<String,String>> data;
    private float handInt,rowInt;
    private SimpleAdapter simpleAdapter;
    private int upOrdown;
    private int  screenWidth,screenHeight;
    private AwesomeButton awesomeButton;
    private ArrayList<DataStation> dataList;
    private Context drawView_context;
    private LinearLayout bgColor;

    public MyDragView(Context context){
        super(context);

    }
    public MyDragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        drawView_context = context;
        data = new ArrayList<>();

        dataList = new ArrayList();

        //利用mInflater 載入DragView的XML檔案
        LayoutInflater mInflater = LayoutInflater.from(context);
        myView = mInflater.inflate(R.layout.dragview, null);
        DisplayMetrics metrics = new DisplayMetrics();
        MapsActivity ma=(MapsActivity)context;
        ma.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        addView(myView);
        intitListView();
    }

    //返回 MyListView
    public MyListView getListView(){
        return listView;
    }

    public LinearLayout getLinearLayout(){
        return  bgColor;
    }

    public AwesomeButton getAwesomeButton(){
        awesomeButton = myView.findViewById(R.id.gotoMap);
        return awesomeButton;
    }
    //初始化MyListView及bgColor
    private void intitListView(){
        listView =myView.findViewById(R.id.listview);
        bgColor = myView.findViewById(R.id.bgColor);
    }
    //DragView 布局的時候 先設定初始位置0;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //這個一開始的高度
        this.setY(screenHeight/2);
    }
    //判斷LISTVIEW 是否置頂
    private boolean isFirstItemVisible() {
        final Adapter adapter = listView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }
        //第一个可见item在ListView中的位置
        if (listView.getFirstVisiblePosition() == 0) {
            //getChildCount是当前屏幕可见范围内的count
            int mostTop = (getChildCount() > 0) ? listView.getChildAt(0)
                    .getTop() : 0;
            if (mostTop >= 0) {
                return true;
            }
        }
        return false;
    }
    //處理滑動時候DragView的效果
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float xxx =rowInt-event.getRawY();
        float moveDiatance =this.getY()-xxx;
        //標註手指移動範圍
        /**
         * screenHeight/2 上限
         * screenHeight-screenHeight/3  下限
         */
        if(moveDiatance>screenHeight/2&&moveDiatance<screenHeight-screenHeight/3) {
            this.setY(moveDiatance);
        }
        //手指手勢向上向下
        if(xxx>0){
            upOrdown =0;
        }else if(xxx<0){
            upOrdown =1;
        }
        if(event.getAction()==MotionEvent.ACTION_UP) {
            if(upOrdown==1){
                Log.v("chad","movedown");
                this.setY(screenHeight-screenHeight/3);
                upOrdown=2;

            }else if (upOrdown==0){
                Log.v("chad","moveup");
                this.setY(screenHeight/2);
                upOrdown=2;
            }
        }
        rowInt = event.getRawY();
        return super.onTouchEvent(event);
    }
    //攔截滑動手勢
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //ACTION_DOWN的時候先記錄 Y 跟RAWY 判斷 移動多少跟向上向下
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            handInt =ev.getY();
            rowInt =ev.getRawY();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float temp = handInt - ev.getY();
            //向上差過20 而且LISTVIEW置頂代表ACTION_MOVE_UP return true 給onTouchEvent;
            if (temp >20 ) {
                if(this.getY()!=screenHeight/2&&isFirstItemVisible()) {
                    return  true;
                }
                //向下差過20 而且LISTVIEW置頂代表ACTION_MOVE_Down return true 給onTouchEvent;
            } else if (temp < -20) {
                if(this.getY()==screenHeight/2&&isFirstItemVisible()) {
                    return  true;
                }
            }
            return false;
        }
        return false;
    }
}
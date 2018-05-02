package tw.org.iii.travelapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.githang.statusbar.StatusBarCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei-chengni on 2018/4/19.
 */

public class LoginPage extends AppCompatActivity{
    private RequestQueue queue;
    private EditText loginaccount,loginpasswd;
    private Button loginbtn,newbtn;
    private String account,passwd;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issign;
    private String memberid;
    private LinearLayout backgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        queue= Volley.newRequestQueue(this);

        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issign = sp.getBoolean("signin",false);
        memberid = sp.getString("member","0");

        loginaccount = findViewById(R.id.login_account);
        loginpasswd = findViewById(R.id.login_passwd);
        loginbtn = findViewById(R.id.login_button);
        newbtn = findViewById(R.id.login_newbutton);
        backgroundColor = findViewById(R.id.login_page_background);
        //登入
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = loginaccount.getText().toString();
                passwd = loginpasswd.getText().toString();
                sighin(account, "",passwd,"normal");
                loginaccount.setText("");
                loginpasswd.setText("");
            }
        });
        //註冊
        newbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://topic-timgyes123.c9users.io/phoneregister.html";
                Intent intent = new Intent(LoginPage.this,
                        PhotoAlbumActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
//                Uri uri = Uri.parse("https://topic-timgyes123.c9users.io/phoneregister.html");
//                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
//                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String backGroundColor = sp.getString("backgroundColor", "#FFFFDD");
        backgroundColor.setBackgroundColor(Color.parseColor(backGroundColor));
    }

    private void sighin(String mail, String name, String password, String type){
        final String p1=mail;
        final String p2=password;
        final String p3=type;
        final String p4=name;
        String url = new MyApplication().url + "/fsit04/app/sighin";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String res = response.trim();
                        if (res.equals("erro")){
                            errortest();
                        }else{
                            try {
                                JSONObject j2 = new JSONObject(res);
                                String mid = j2.getString("id");
                                editor.putBoolean("signin",true);
                                editor.putString("memberid",mid);
                                editor.commit();
                                Intent intent =new Intent(getApplicationContext(),HomePageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(LoginPage.this,
                                        "登入成功", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> m1 =new HashMap<>();
                m1.put("mail",p1);
                m1.put("password", p2);
                m1.put("type",p3);
                m1.put("name",p4);
                return m1;
            }
        };
        queue.add(stringRequest);
    }

    private void errortest(){
        new AlertDialog.Builder(LoginPage.this)
                .setTitle(" ")
                .setMessage("輸入帳號或密碼錯誤")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(LoginPage.this,
                                "請重新輸入帳號密碼",
                                Toast.LENGTH_SHORT).show();
//                        finish();
                    }
                }).show();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
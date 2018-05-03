package tw.org.iii.travelapp;

import android.app.Application;

/**
 * Created by MacGyver on 2018/4/30.
 */

public class MyApplication extends Application{

    public String url, user_id;

    public MyApplication() {
        url = "http://118.170.11.89:8080";
        user_id = "5";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

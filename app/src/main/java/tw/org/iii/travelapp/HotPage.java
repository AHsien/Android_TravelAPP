package tw.org.iii.travelapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class HotPage extends ListFragment {
    private LinkedList<AttrListModel> data;
    private ListView listView;
    private String jstring;
    private JSONObject jsonObject;
    private MyhotlistAdapter adapter;
    private ImageView mesbtn,addbtn;
    private float screenWidth,screenHeight,newHeight;
    private RequestQueue queue;
    private FrameLayout backgroundColor;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issignin;
    private String memberid;
    private String memberemail;
    private ViewHolder holder;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)    {
        queue= Volley.newRequestQueue(getContext());
        View v = inflater.inflate(R.layout.fragment_hot_page,container,false);
        listView=(ListView)v.findViewById(android.R.id.list);
        backgroundColor = (FrameLayout)v.findViewById(R.id.hot_background);
        new attrHttpasync().execute();
        sp = getActivity().getSharedPreferences("memberdata",Context.MODE_PRIVATE);
        editor = sp.edit();
        issignin = sp.getBoolean("signin",false);
        memberid = sp.getString("memberid","");
        memberemail = sp.getString("memberemail","");
        Log.v("grey","hotsign="+issignin);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        String backGroundColor = sp.getString("backgroundColor", "#FFFFDD");
        backgroundColor.setBackgroundColor(Color.parseColor(backGroundColor));
    }

    private class attrHttpasync extends AsyncTask<String, Void, LinkedList<AttrListModel>> {

        @Override
        protected LinkedList<AttrListModel> doInBackground(String... strings) {
            JSONArray jsonArray = null;
            data = new LinkedList<>();
            jstring = JSONFuction.getJSONFromurl(
                    new MyApplication().url + "/fsit04/User_favorite?user_id=3");
            try {
                jsonArray = new JSONArray(jstring);
                for(int i=0;i<jsonArray.length();i++){
                    ArrayList<String> photo_url = new ArrayList<>();
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    JSONArray imgarray = jsonObject2.getJSONArray("Img");
                    for(int y = 0; y < imgarray.length(); y++){
                        String imgUrl = imgarray.getJSONObject(y).getString("url");
                        photo_url.add(imgUrl);
                    }
                    JSONObject jsonObject3 = imgarray.getJSONObject(0);
                    AttrListModel listModel = new AttrListModel();
                    listModel.setAid(jsonObject2.getString("total_id"));
                    listModel.setName(jsonObject2.getString("name"));
                    listModel.setAddress(jsonObject2.getString("address"));
                    listModel.setOpentime(jsonObject2.getString("MEMO_TIME"));
                    listModel.setDescription(jsonObject2.getString("xbody"));
                    listModel.setImgs(jsonObject3.getString("url"));
                    listModel.setLat(jsonObject2.getDouble("lat"));
                    listModel.setLng(jsonObject2.getDouble("lng"));
                    listModel.setPhoto_url(photo_url);
                    data.add(listModel);
                }
                return data;
            } catch (Exception e) {
                Log.v("grey","error22 = " + e.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(LinkedList jsonresult) {
            super.onPostExecute(jsonresult);
            adapter = new MyhotlistAdapter(getActivity(),data);
            setListAdapter(adapter);
        }
    }
    public class MyhotlistAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private LinkedList<AttrListModel> data;
        private AttrListModel reslut = new AttrListModel();
        private TextView itemtitle;
        private TextView itemaddr;
        private ImageView itemimage;

        public MyhotlistAdapter(Context context,
                                LinkedList<AttrListModel> linklist) {
            this.context = context;
            this.data = linklist;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            reslut = data.get(position);
            if(view==null){
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.item_layout_hot,viewGroup,false);
                inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder.itemtitle = (TextView)view.findViewById(R.id.item_title);
                holder.itemaddress = (TextView)view.findViewById(R.id.item_addr);
                holder.itemimage = (ImageView)view.findViewById(R.id.item_image);

                view.setTag(holder);
                holder.mesbtn = view.findViewById(R.id.item_message_btn);
                holder.addbtn = view.findViewById(R.id.item_add_btn);
//                Log.v("grey","resaid = "+reslut.getAid());
            }else{
                holder = (ViewHolder) view.getTag();
            }
            reslut = data.get(position);
            //set reslut to textview
            holder.itemtitle.setText(reslut.getName());
            holder.itemaddress.setText(reslut.getAddress());
            GlideApp.with(context)
                    .load(reslut.getImgs())
//                    .override((int)screenWidth, (int)newHeight)
                    .into(holder.itemimage);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reslut = data.get(position);
                    Intent intent = new Intent(getActivity(),DetailActivity.class);
                    intent.putExtra("total_id",reslut.getAid());
                    intent.putExtra("stitle",reslut.getName());
                    intent.putExtra("address",reslut.getAddress());
                    intent.putExtra("img_url",reslut.getImgs());
                    intent.putExtra("xbody",reslut.getDescription());
                    intent.putExtra("memo_time",reslut.getOpentime());
                    intent.putExtra("lat", reslut.getLat());
                    intent.putExtra("lng", reslut.getLng());
                    intent.putStringArrayListExtra("photos", reslut.getPhoto_url());
                    startActivity(intent);
                }
            });
            //收藏
            holder.addbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    issignin = sp.getBoolean("signin", false);
                    if (issignin==true){
                        reslut = data.get(position);
                        addFavorite(memberid,reslut.getAid());
                        PrettyDialog dialog = new PrettyDialog(getContext());
                        dialog.setTitle("成功加入我的最愛")
                                .setIconTint(R.color.pdlg_color_gray)
                                .show();
                    }else {
                        Intent intent = new Intent(getActivity(),LoginActivity.class);
                        startActivity(intent);
                    }

                }
            });
            //留言
            holder.mesbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = new MyApplication().url +
                            "/fsit04/attractions?total_id=" +
                            data.get(position).getAid();
                    Intent intent = new Intent(getActivity(),
                            PhotoAlbumActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            });
            return view;
        }
    }

    private void showAletDialog(){
        DialogFragment newFragment = AlertDialogFragment.newInstance(1);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public class ViewHolder
    {
        public ImageView itemimage;
        public TextView itemtitle;
        public TextView itemaddress;
        public ImageView mesbtn;
        public ImageView addbtn;
    }

    private void addFavorite(String user_id,String total_id){
        String url = new MyApplication().url + "/fsit04/User_favorite";

        final String p1 =user_id;
        final String p2=total_id;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> m1 =new HashMap<>();
                m1.put("user_id",p1);
                m1.put("total_id", p2);
                return m1;
            }
        };
        queue.add(stringRequest);

    }

}
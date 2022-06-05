package com.termproject.metaverse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;

    RecyclerView recyclerMap = null;
    RecyclerViewAdapter mapAdapter = null;
    ArrayList<RecyclerViewItem> list = new ArrayList<>();
    ArrayList<String> mapList = new ArrayList<>();
    ArrayList<String> map = new ArrayList<>();

    RecyclerView recyclerMyMap = null;
    RecyclerViewAdapter myMapAdapter = null;
    ArrayList<RecyclerViewItem> myList = new ArrayList<>();
    ArrayList<String> myMapList = new ArrayList<>();

    ImageView myAvatar;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> nicknameList = new ArrayList<>();
    ArrayList<String> avatarList = new ArrayList<>();

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "choose_map";

    String jsonString;

    public FragmentHome() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container,false);

        myAvatar = (ImageView) view.findViewById(R.id.myAvatar);

        recyclerMap = view.findViewById(R.id.recyclerMap);
        recyclerMap.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        mapAdapter = new RecyclerViewAdapter(list);
        recyclerMap.setAdapter(mapAdapter);

        recyclerMyMap = view.findViewById(R.id.recyclerMyMAP);
        recyclerMyMap.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        myMapAdapter = new RecyclerViewAdapter(myList);
        recyclerMyMap.setAdapter(myMapAdapter);

        FragmentHome.GetData task = new FragmentHome.GetData();
        task.execute("http://" + IP_ADDRESS + "/map.php");

        FragmentHome.GetMyData myTask = new FragmentHome.GetMyData();
        myTask.execute("http://" + IP_ADDRESS + "/myMap.php");

        FragmentHome.GetAvatarData avatarTask = new FragmentHome.GetAvatarData();
        avatarTask.execute("http://" + IP_ADDRESS + "/mem_list.php");

        ImageView add = (ImageView) view.findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateMapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });  //비회원이면 접근불가 팝업, 회원이면 넘어가도록 수정 필요

        return view;

    }

    public void addItem(Drawable icon){
        RecyclerViewItem item = new RecyclerViewItem();

        item.setIcon(icon);

        list.add(item);
    }

    public void addMyItem(Drawable icon){
        RecyclerViewItem item = new RecyclerViewItem();

        item.setIcon(icon);

        myList.add(item);
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showMapResult();
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    private void showMapResult() {

        String TAG_JSON = "map";
        String TAG_MAP = "mname";
        String TAG_ACCESS = "access";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String mapName = item.getString(TAG_MAP);
                String access = item.getString(TAG_ACCESS);

                mapList.add(mapName);

            }


            for(int i=0;i<mapList.size();i++) {
                int a = getResources().getIdentifier(mapList.get(i), "drawable", getActivity().getPackageName());
                Drawable da = ResourcesCompat.getDrawable(getResources(),a,null);
                String downMap = mapList.get(i).substring(4);
                String upMap = downMap.substring(0,1).toUpperCase()+downMap.substring(1);
                map.add(upMap);
                addItem(da);
            }

            mapAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    RecyclerViewItem item = list.get(position);
                    Drawable img = item.getIcon();
                    Intent intent;

                    if (position == 0) {
                        intent = new Intent(getActivity(), ClassroomActivity.class);
                    } else if (position == 1) {
                        intent = new Intent(getActivity(), GameActivity.class);
                    } else if (position == 2) {
                        intent = new Intent(getActivity(), OfficeActivity.class);
                    } else {
                        intent = new Intent(getActivity(), SnowActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                }
            });

            mapAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetMyData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showMyMapResult();
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "GetMyData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    private void showMyMapResult() {

        String TAG_JSON = "map";
        String TAG_MAP = "mname";
        String TAG_ACCESS = "access";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String mapName = item.getString(TAG_MAP);
                String access = item.getString(TAG_ACCESS);

                myMapList.add(mapName);

            }

            for(int i=0;i<myMapList.size();i++) {
                String imgPath = getActivity().getCacheDir() + "/" + myMapList.get(i);
                Bitmap bm = BitmapFactory.decodeFile(imgPath);
                Drawable d = new BitmapDrawable(bm);
                addMyItem(d);
            }

            myMapAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    RecyclerViewItem item = myList.get(position);
                    Intent intent;
                    intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra("map", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

            myMapAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetAvatarData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showAvatarResult();
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    private void showAvatarResult() {

        String TAG_JSON = "mem_list";
        String TAG_EMAIL = "email";
        String TAG_NAME = "nickname";
        String TAG_AVATAR = "avatar";

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM USER", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            strId = c.getString(0);
            c.moveToNext();
        }
        c.close();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String email = item.getString(TAG_EMAIL);
                String nickname = item.getString(TAG_NAME);
                String avatar = item.getString(TAG_AVATAR);

                emailList.add(email);
                nicknameList.add(nickname);
                avatarList.add(avatar);

            }

            int index = emailList.indexOf(strId);
            String a = avatarList.get(index);
            int da = getResources().getIdentifier(a, "drawable", getActivity().getPackageName());
            Drawable d = ResourcesCompat.getDrawable(getResources(),da,null);

            myAvatar.setImageDrawable(d);

            myAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}

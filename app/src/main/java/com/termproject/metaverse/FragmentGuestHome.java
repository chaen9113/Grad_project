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
import android.widget.Toast;

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

public class FragmentGuestHome extends Fragment {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String gID;

    RecyclerView recyclerMap = null;
    RecyclerViewAdapter mapAdapter = null;
    ArrayList<RecyclerViewItem> list = new ArrayList<>();
    ArrayList<String> mapList = new ArrayList<>();
    ArrayList<String> map = new ArrayList<>();

    RecyclerView recyclerCreatedMap = null;
    RecyclerViewAdapter createdAdapter = null;
    ArrayList<RecyclerViewItem> createdList = new ArrayList<>();
    ArrayList<String> createdMapList = new ArrayList<>();

    ImageView guestAvatar;
    ArrayList<String> g_numList = new ArrayList<>();
    ArrayList<String> g_nameList = new ArrayList<>();
    ArrayList<String> g_avatarList = new ArrayList<>();

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "choose_map";

    String jsonString;

    public FragmentGuestHome() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guesthome, container,false);

        guestAvatar = (ImageView) view.findViewById(R.id.guest_myAvatar);

        recyclerMap = view.findViewById(R.id.guest_recyclerMap);
        recyclerMap.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        mapAdapter = new RecyclerViewAdapter(list);
        recyclerMap.setAdapter(mapAdapter);

        recyclerCreatedMap = view.findViewById(R.id.guest_recyclerCreatedMAP);
        recyclerCreatedMap.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        createdAdapter = new RecyclerViewAdapter(createdList);
        recyclerCreatedMap.setAdapter(createdAdapter);

        FragmentGuestHome.GetData task = new FragmentGuestHome.GetData();
        task.execute("http://" + IP_ADDRESS + "/map.php");

        FragmentGuestHome.GetCreatedData createdTask = new FragmentGuestHome.GetCreatedData();
        createdTask.execute("http://" + IP_ADDRESS + "/myMap.php");

        FragmentGuestHome.GetAvatarData g_avatarTask = new FragmentGuestHome.GetAvatarData();
        g_avatarTask.execute("http://" + IP_ADDRESS + "/guest.php");

        ImageView add = (ImageView) view.findViewById(R.id.guest_add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity().getApplicationContext(), "비회원은 사용 불가합니다.", Toast.LENGTH_LONG).show();
            }
        });

        return view;

    }

    public void addItem(Drawable icon){
        RecyclerViewItem item = new RecyclerViewItem();

        item.setIcon(icon);

        list.add(item);
    }

    public void addCreatedItem(Drawable icon){
        RecyclerViewItem item = new RecyclerViewItem();

        item.setIcon(icon);

        createdList.add(item);
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
                    intent = new Intent(getActivity(), GuestMapActivity.class);
                    intent.putExtra("map", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

            mapAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetCreatedData extends AsyncTask<String, Void, String> {
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

                createdMapList.add(mapName);

            }

            for(int i=0;i<createdMapList.size();i++) {
                String imgPath = getActivity().getCacheDir() + "/" + createdMapList.get(i);
                Bitmap bm = BitmapFactory.decodeFile(imgPath);
                Drawable d = new BitmapDrawable(bm);
                addCreatedItem(d);
            }

            createdAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    RecyclerViewItem item = createdList.get(position);
                    Intent intent;
                    intent = new Intent(getActivity(), GuestCreatedMapActivity.class);
                    intent.putExtra("map", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

            createdAdapter.notifyDataSetChanged();

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

        String TAG_JSON = "guest";
        String TAG_NUM = "gnum";
        String TAG_NAME = "gname";
        String TAG_AVATAR = "gavatar";

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM GUEST", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            gID = c.getString(0);
            c.moveToNext();
        }
        c.close();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String g_num = item.getString(TAG_NUM);
                String g_name = item.getString(TAG_NAME);
                String g_avatar = item.getString(TAG_AVATAR);

                g_numList.add(g_num);
                g_nameList.add(g_name);
                g_avatarList.add(g_avatar);

            }

            int index = g_nameList.indexOf(gID);
            String a = g_avatarList.get(index);
            int da = getResources().getIdentifier(a, "drawable", getActivity().getPackageName());
            Drawable d = ResourcesCompat.getDrawable(getResources(),da,null);

            guestAvatar.setImageDrawable(d);

            guestAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext(), "비회원은 사용 불가합니다.", Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}

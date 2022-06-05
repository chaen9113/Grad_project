package com.termproject.metaverse;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GuestMapActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String gID;
    float x, y;

    ImageView guestAvatar;
    ArrayList<String> g_nameList = new ArrayList<>();
    ArrayList<String> g_avatarList = new ArrayList<>();
    ArrayList<String> g_frontList = new ArrayList<>();
    ArrayList<String> g_backList = new ArrayList<>();
    ArrayList<String> g_leftList = new ArrayList<>();
    ArrayList<String> g_rightList = new ArrayList<>();

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "guest_map";

    String jsonString;
    ArrayList<String> guestMap = new ArrayList<>();

    RelativeLayout guestMapLayout;
    Button up, down, left, right;
    int position;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guestmap);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        position = intent.getIntExtra("map",0);

        guestMapLayout = (RelativeLayout) findViewById(R.id.guestMapLayout);

        guestAvatar = (ImageView) findViewById(R.id.mapAvatar);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);


        GuestMapActivity.GetData task = new GuestMapActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/map.php");

        GuestMapActivity.GetAvatarData g_avatarTask = new GuestMapActivity.GetAvatarData();
        g_avatarTask.execute("http://" + IP_ADDRESS + "/map_guest.php");
    }


    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(GuestMapActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showResult();
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

    private void showResult() {

        String TAG_JSON = "map";
        String TAG_MAP = "mname";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String map_name = item.getString(TAG_MAP);

                guestMap.add(map_name);

            }

            String gm = guestMap.get(position);
            int ga = getResources().getIdentifier(gm, "drawable", getPackageName());
            Drawable d = ResourcesCompat.getDrawable(getResources(),ga,null);
            guestMapLayout.setBackground(d);

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

            progressDialog = ProgressDialog.show(GuestMapActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "map_guest";
        String TAG_NAME = "gname";
        String TAG_AVATAR = "gavatar";
        String TAG_FRONT = "front";
        String TAG_BACK = "back";
        String TAG_LEFT = "left";
        String TAG_RIGHT = "right";

        dbHelper = new DBHelper(this);
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
                String name = item.getString(TAG_NAME);
                String avatar = item.getString(TAG_AVATAR);
                String front = item.getString(TAG_FRONT);
                String back = item.getString(TAG_BACK);
                String left = item.getString(TAG_LEFT);
                String right = item.getString(TAG_RIGHT);

                g_nameList.add(name);
                g_avatarList.add(avatar);
                g_frontList.add(front);
                g_backList.add(back);
                g_leftList.add(left);
                g_rightList.add(right);

            }

            int index = g_nameList.indexOf(gID);
            String a = g_avatarList.get(index);
            int da = getResources().getIdentifier(a, "drawable", getPackageName());
            Drawable d = ResourcesCompat.getDrawable(getResources(),da,null);

            guestAvatar.setImageDrawable(d);

            up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int b = getResources().getIdentifier(g_backList.get(index), "drawable", getPackageName());
                    Drawable db = ResourcesCompat.getDrawable(getResources(),b,null);
                    guestAvatar.setImageDrawable(db);
                    x = guestAvatar.getX();
                    y = guestAvatar.getY();
                    y -= 20;
                    guestAvatar.setX(x);
                    guestAvatar.setY(y);
                }
            });


            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int f = getResources().getIdentifier(g_frontList.get(index), "drawable", getPackageName());
                    Drawable df = ResourcesCompat.getDrawable(getResources(),f,null);
                    guestAvatar.setImageDrawable(df);
                    x = guestAvatar.getX();
                    y = guestAvatar.getY();
                    y += 20;
                    guestAvatar.setX(x);
                    guestAvatar.setY(y);
                }
            });


            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int l = getResources().getIdentifier(g_leftList.get(index), "drawable", getPackageName());
                    Drawable dl = ResourcesCompat.getDrawable(getResources(),l,null);
                    guestAvatar.setImageDrawable(dl);
                    x = guestAvatar.getX();
                    y = guestAvatar.getY();
                    x -= 20;
                    guestAvatar.setX(x);
                    guestAvatar.setY(y);
                }
            });


            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int r = getResources().getIdentifier(g_rightList.get(index), "drawable", getPackageName());
                    Drawable dr = ResourcesCompat.getDrawable(getResources(),r,null);
                    guestAvatar.setImageDrawable(dr);
                    x = guestAvatar.getX();
                    y = guestAvatar.getY();
                    x += 20;
                    guestAvatar.setX(x);
                    guestAvatar.setY(y);
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}

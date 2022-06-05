package com.termproject.metaverse;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

public class OfficeActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;
    float x, y;

    String jsonString;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> avatarList = new ArrayList<>();
    ArrayList<String> frontList = new ArrayList<>();
    ArrayList<String> backList = new ArrayList<>();
    ArrayList<String> leftList = new ArrayList<>();
    ArrayList<String> rightList = new ArrayList<>();

    ImageView mapAvatar;
    Button up, down, left, right;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "map_avatar";

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_office);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mapAvatar = (ImageView) findViewById(R.id.mapAvatar);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);

        OfficeActivity.GetAvatarData avatarTask = new OfficeActivity.GetAvatarData();
        avatarTask.execute("http://" + IP_ADDRESS + "/map_avatar.php");
    }

    private class GetAvatarData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(OfficeActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "map_avatar";
        String TAG_EMAIL = "email";
        String TAG_AVATAR = "avatar";
        String TAG_FRONT = "front";
        String TAG_BACK = "back";
        String TAG_LEFT = "left";
        String TAG_RIGHT = "right";

        dbHelper = new DBHelper(this);
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
                String avatar = item.getString(TAG_AVATAR);
                String front = item.getString(TAG_FRONT);
                String back = item.getString(TAG_BACK);
                String left = item.getString(TAG_LEFT);
                String right = item.getString(TAG_RIGHT);

                emailList.add(email);
                avatarList.add(avatar);
                frontList.add(front);
                backList.add(back);
                leftList.add(left);
                rightList.add(right);

            }

            int index = emailList.indexOf(strId);
            String a = avatarList.get(index);
            int da = getResources().getIdentifier(a, "drawable", getPackageName());
            Drawable d = ResourcesCompat.getDrawable(getResources(),da,null);

            mapAvatar.setImageDrawable(d);

            up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int b = getResources().getIdentifier(backList.get(index), "drawable", getPackageName());
                    Drawable db = ResourcesCompat.getDrawable(getResources(),b,null);
                    mapAvatar.setImageDrawable(db);
                    x = mapAvatar.getX();
                    y = mapAvatar.getY();
                    y -= 20;
                    mapAvatar.setX(x);
                    mapAvatar.setY(y);
                }
            });


            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int f = getResources().getIdentifier(frontList.get(index), "drawable", getPackageName());
                    Drawable df = ResourcesCompat.getDrawable(getResources(),f,null);
                    mapAvatar.setImageDrawable(df);
                    x = mapAvatar.getX();
                    y = mapAvatar.getY();
                    y += 20;
                    mapAvatar.setX(x);
                    mapAvatar.setY(y);
                }
            });


            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int l = getResources().getIdentifier(leftList.get(index), "drawable", getPackageName());
                    Drawable dl = ResourcesCompat.getDrawable(getResources(),l,null);
                    mapAvatar.setImageDrawable(dl);
                    x = mapAvatar.getX();
                    y = mapAvatar.getY();
                    x -= 20;
                    mapAvatar.setX(x);
                    mapAvatar.setY(y);
                }
            });


            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int r = getResources().getIdentifier(rightList.get(index), "drawable", getPackageName());
                    Drawable dr = ResourcesCompat.getDrawable(getResources(),r,null);
                    mapAvatar.setImageDrawable(dr);
                    x = mapAvatar.getX();
                    y = mapAvatar.getY();
                    x += 20;
                    mapAvatar.setX(x);
                    mapAvatar.setY(y);
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}

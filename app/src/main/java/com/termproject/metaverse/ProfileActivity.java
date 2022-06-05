package com.termproject.metaverse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ProfileActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;
    ImageView profAvatar;
    TextView profName, myFollower, myFollowing;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "profile";

    String jsonString;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> nicknameList = new ArrayList<>();
    ArrayList<String> avatarList = new ArrayList<>();

    ArrayList<String> followerEmail = new ArrayList<>();
    ArrayList<String> followerNum = new ArrayList<>();

    ArrayList<String> followingEmail = new ArrayList<>();
    ArrayList<String> followingNum = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM USER", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            strId = c.getString(0);
            c.moveToNext();
        }
        c.close();

        profName = findViewById(R.id.profName);
        profAvatar = findViewById(R.id.profAvatar);
        myFollowing = findViewById(R.id.following);
        myFollower = findViewById(R.id.follower);

        ProfileActivity.GetData task = new ProfileActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/mem_list.php");

        ProfileActivity.GetFollowerNum followerTask = new ProfileActivity.GetFollowerNum();
        followerTask.execute("http://" + IP_ADDRESS + "/follower.php");

        ProfileActivity.GetFollowingNum followingTask = new ProfileActivity.GetFollowingNum();
        followingTask.execute("http://" + IP_ADDRESS + "/following.php");
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ProfileActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "mem_list";
        String TAG_EMAIL = "email";
        String TAG_NAME = "nickname";
        String TAG_AVATAR = "avatar";

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
            String n = nicknameList.get(index);
            String a = avatarList.get(index);
            int da = getResources().getIdentifier(a, "drawable", getPackageName());
            Drawable d = getDrawable(da);

            profAvatar.setImageDrawable(d);
            profName.setText(n);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetFollowerNum extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ProfileActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showFollower();
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

    private void showFollower() {

        String TAG_JSON = "follower";
        String TAG_EMAIL = "f_email";
        String TAG_FOLLOWER = "num_follower";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String email = item.getString(TAG_EMAIL);
                String num = item.getString(TAG_FOLLOWER);

                followerEmail.add(email);
                followerNum.add(num);
            }

            int index = followerEmail.indexOf(strId);
            String count = followerNum.get(index);
            myFollower.setText(count);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetFollowingNum extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ProfileActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showFollowing();
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

    private void showFollowing() {

        String TAG_JSON = "following";
        String TAG_EMAIL = "email";
        String TAG_FOLLOWING = "num_following";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String email = item.getString(TAG_EMAIL);
                String num = item.getString(TAG_FOLLOWING);

                followingEmail.add(email);
                followingNum.add(num);
            }

            int index = followingEmail.indexOf(strId);
            String count = followingNum.get(index);
            myFollowing.setText(count);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}

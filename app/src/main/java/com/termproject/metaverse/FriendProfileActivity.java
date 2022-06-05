package com.termproject.metaverse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

public class FriendProfileActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;

    ImageView friendAvatar;
    TextView friendName, friendFollower, friendFollowing;
    Button follow, cancel;
    String friend;
    int position;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "friend_profile";

    String jsonString;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> nicknameList = new ArrayList<>();
    ArrayList<String> avatarList = new ArrayList<>();

    ArrayList<String> followerEmail = new ArrayList<>();
    ArrayList<String> followerNum = new ArrayList<>();

    ArrayList<String> followingEmail = new ArrayList<>();
    ArrayList<String> followingNum = new ArrayList<>();

    ArrayList<String> myEmail = new ArrayList<>();
    ArrayList<String> fEmail = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendprofile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        position = intent.getIntExtra("pos", 0);
        friend = intent.getStringExtra("friend");

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM USER", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            strId = c.getString(0);
            c.moveToNext();
        }
        c.close();

        friendName = (TextView) findViewById(R.id.friend_name);
        friendAvatar = (ImageView) findViewById(R.id.friend_avatar);
        friendFollower = (TextView) findViewById(R.id.friend_follower);
        friendFollowing = (TextView) findViewById(R.id.friend_following);
        follow = (Button) findViewById(R.id.follow);
        cancel = (Button) findViewById(R.id.cancel);

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                follow.setText("팔로잉");
                follow.setBackgroundColor(Color.parseColor("#E5E5E5"));
                follow.setTextColor(Color.BLACK);
                follow.setClickable(false);
                insertFriend(strId, friend);
                cancel.setBackgroundColor(Color.parseColor("#674D83"));
                cancel.setTextColor(Color.WHITE);
                cancel.setClickable(true);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                follow.setText("팔로우");
                follow.setBackgroundColor(Color.parseColor("#674D83"));
                follow.setTextColor(Color.WHITE);
                follow.setClickable(true);
                deleteFriend(strId, friend);
                cancel.setBackgroundColor(Color.parseColor("#E5E5E5"));
                cancel.setTextColor(Color.BLACK);
                cancel.setClickable(false);
            }
        });

        FriendProfileActivity.GetData task = new FriendProfileActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/mem_list.php");

        FriendProfileActivity.GetFollowerNum followerTask = new FriendProfileActivity.GetFollowerNum();
        followerTask.execute("http://" + IP_ADDRESS + "/follower.php");

        FriendProfileActivity.GetFollowingNum followingTask = new FriendProfileActivity.GetFollowingNum();
        followingTask.execute("http://" + IP_ADDRESS + "/following.php");

        FriendProfileActivity.GetFriend friendTask = new FriendProfileActivity.GetFriend();
        friendTask.execute("http://" + IP_ADDRESS + "/friend.php");
    }


    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
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
            emailList.remove(index);
            nicknameList.remove(index);
            avatarList.remove(index);

            String el = emailList.get(position);
            String nl = nicknameList.get(position);
            String al = avatarList.get(position);
            int da = getResources().getIdentifier(al, "drawable", getPackageName());
            Drawable d = getDrawable(da);

            friendAvatar.setImageDrawable(d);
            friendName.setText(nl);


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

            progressDialog = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
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

            int index = followerEmail.indexOf(friend);
            String count = followerNum.get(index);
            friendFollower.setText(count);

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

            progressDialog = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
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

            int index = followingEmail.indexOf(friend);
            String count = followingNum.get(index);
            friendFollowing.setText(count);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetFriend extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showFriend();
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

    private void showFriend() {

        String TAG_JSON = "friend";
        String TAG_EMAIL = "email";
        String TAG_FEMAIL = "f_email";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String email = item.getString(TAG_EMAIL);
                String f_email = item.getString(TAG_FEMAIL);

                if (email.equals(strId) && f_email.equals(friend)) {
                    myEmail.add(email);
                    fEmail.add(f_email);
                }
            }

            if (myEmail.indexOf(strId) == -1) {
                follow.setText("팔로우");
                follow.setBackgroundColor(Color.parseColor("#674D83"));
                follow.setTextColor(Color.WHITE);
                follow.setClickable(true);
                cancel.setBackgroundColor(Color.parseColor("#E5E5E5"));
                cancel.setTextColor(Color.BLACK);
                cancel.setClickable(false);
            } else {
                follow.setText("팔로잉");
                follow.setBackgroundColor(Color.parseColor("#E5E5E5"));
                follow.setTextColor(Color.BLACK);
                follow.setClickable(false);
                cancel.setBackgroundColor(Color.parseColor("#674D83"));
                cancel.setTextColor(Color.WHITE);
                cancel.setClickable(true);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private void insertFriend (String e, String f) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Log.d("Tag : ", s);
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String eText = (String) params[0];
                    String fText = (String) params[1];

                    String link = "http://"+ IP_ADDRESS +"/insert_friend.php";
                    String data = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(eText, "UTF-8");
                    data += "&" + URLEncoder.encode("f_email", "UTF-8") + "=" + URLEncoder.encode(fText, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                    outputStreamWriter.write(data);
                    outputStreamWriter.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    Log.d("tag: ", sb.toString());
                    return sb.toString();

                } catch (Exception e) {
                    return new String ("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(e, f);
    }

    private void deleteFriend (String e, String f) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(FriendProfileActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Log.d("Tag : ", s);
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String eText = (String) params[0];
                    String fText = (String) params[1];

                    String link = "http://"+ IP_ADDRESS +"/delete_friend.php";
                    String data = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(eText, "UTF-8");
                    data += "&" + URLEncoder.encode("f_email", "UTF-8") + "=" + URLEncoder.encode(fText, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                    outputStreamWriter.write(data);
                    outputStreamWriter.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    Log.d("tag: ", sb.toString());
                    return sb.toString();

                } catch (Exception e) {
                    return new String ("Exception: " + e.getMessage());
                }
            }
        }
        DeleteData task = new DeleteData();
        task.execute(e, f);
    }
}

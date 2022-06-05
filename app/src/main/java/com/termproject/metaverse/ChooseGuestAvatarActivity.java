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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ChooseGuestAvatarActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Button selectGuestAvatar;
    ImageView checkGuestAvatar;
    TextView guestID, guestNO;
    int pos;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "choose_guest_avatar";

    String jsonString;
    ArrayList<String> avatar = new ArrayList<>();
    ArrayList<String> count = new ArrayList<>();

    RecyclerView recyclerGuestAvatar = null;
    RecyclerViewAdapter guestAvatarAdapter = null;
    ArrayList<RecyclerViewItem> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseguestavatar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        guestID = findViewById(R.id.guestID);
        guestNO = findViewById(R.id.guestNO);

        recyclerGuestAvatar = findViewById(R.id.recyclerGuestAvatar);
        recyclerGuestAvatar.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        checkGuestAvatar = (ImageView) findViewById(R.id.checkGuestAvatar);
        selectGuestAvatar = (Button) findViewById(R.id.selectGuestAvatar);

        ChooseGuestAvatarActivity.GetData task = new ChooseGuestAvatarActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/avatar.php");

        ChooseGuestAvatarActivity.GetGuest guestTask = new ChooseGuestAvatarActivity.GetGuest();
        guestTask.execute("http://" + IP_ADDRESS + "/guest_count.php");

    }

    public void addItem(Drawable icon){
        RecyclerViewItem item = new RecyclerViewItem();

        item.setIcon(icon);

        list.add(item);
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ChooseGuestAvatarActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "avatar_info";
        String TAG_AVATAR = "avatar";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String avatar_name = item.getString(TAG_AVATAR);

                avatar.add(avatar_name);

            }

            for(int i=0;i<avatar.size();i++) {
                int a = getResources().getIdentifier(avatar.get(i), "drawable", getPackageName());
                addItem(getDrawable(a));
            }

            guestAvatarAdapter = new RecyclerViewAdapter(list);
            recyclerGuestAvatar.setAdapter(guestAvatarAdapter);

            guestAvatarAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    RecyclerViewItem item = list.get(position);
                    Drawable img = item.getIcon();
                    checkGuestAvatar.setImageDrawable(img);
                    pos = position;
                }
            });

            selectGuestAvatar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ChooseGuestAvatarActivity.this, GuestHomeActivity.class);
                    String t = guestID.getText().toString() + guestNO.getText().toString();
                    db.execSQL("INSERT INTO GUEST VALUES ('" + t + "');");
                    deleteGuest(t);
                    insertGuest(t, avatar.get(pos));
                    startActivity(intent);
                }
            });

            guestAvatarAdapter.notifyDataSetChanged();


        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class GetGuest extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ChooseGuestAvatarActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);
            jsonString = result;
            showGuestResult();
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

    private void showGuestResult() {

        String TAG_JSON = "guest_count";
        String TAG_COUNT = "count";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String g_count = item.getString(TAG_COUNT);

                count.add(g_count);

            }

            guestNO.setText(count.get(0));

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private void insertGuest (final String n, String a) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ChooseGuestAvatarActivity.this, "Please Wait", null, true, true);
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
                    String nText = (String) params[0];
                    String aText = (String) params[1];

                    String link = "http://"+ IP_ADDRESS +"/insert_guest.php";
                    String data = URLEncoder.encode("gname","UTF-8") + "=" + URLEncoder.encode(nText, "UTF-8");
                    data += "&" + URLEncoder.encode("gavatar", "UTF-8") + "=" + URLEncoder.encode(aText, "UTF-8");

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
        task.execute(n, a);
    }

    private void deleteGuest (final String n) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ChooseGuestAvatarActivity.this, "Please Wait", null, true, true);
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
                    String nText = (String) params[0];

                    String link = "http://"+ IP_ADDRESS +"/delete_guest.php";
                    String data = URLEncoder.encode("gname","UTF-8") + "=" + URLEncoder.encode(nText, "UTF-8");

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
        task.execute(n);
    }
}

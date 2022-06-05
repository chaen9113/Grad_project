package com.termproject.metaverse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FragmentFriend extends Fragment {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "friend_list";

    String jsonString;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> nicknameList = new ArrayList<>();
    ArrayList<String> avatarList = new ArrayList<>();

    ListView list;
    ListItemAdapter adapter;

    public FragmentFriend() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container,false);

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM USER", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            strId = c.getString(0);
            c.moveToNext();
        }
        c.close();

        list = (ListView) view.findViewById(R.id.list);
        adapter = new ListItemAdapter();

        FragmentFriend.GetData task = new FragmentFriend.GetData();
        task.execute("http://" + IP_ADDRESS + "/mem_list.php");

        EditText editTextFilter = (EditText) view.findViewById(R.id.search);
        editTextFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable edit) {
                String filterText = edit.toString();

                ((ListItemAdapter)list.getAdapter()).getFilter().filter(filterText);
            }

            @Override
            public void beforeTextChanged(CharSequence cs, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
            }
        });


        return view;
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

            for(int i=0;i<emailList.size();i++) {
                String n = nicknameList.get(i);
                int a = getResources().getIdentifier(avatarList.get(i), "drawable", getActivity().getPackageName());
                adapter.addItem(getActivity().getDrawable(a), n);
            }

            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    ListItem item = (ListItem) parent.getItemAtPosition(position);
                    String str = emailList.get(position);
                    Intent intent;
                    intent = new Intent(getActivity(), FriendProfileActivity.class);
                    intent.putExtra("pos", position);
                    intent.putExtra("friend", str);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

}

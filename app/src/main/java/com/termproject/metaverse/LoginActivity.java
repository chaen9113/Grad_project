package com.termproject.metaverse;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String count;
    EditText email, password;
    Button completeLogin, goJoin, nonMember;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "login";

    String jsonString;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<String> passwordList = new ArrayList<>();
    ArrayList<String> maList = new ArrayList<>();
    ArrayList<String> oxList = new ArrayList<>();
    ArrayList<String> phoneList = new ArrayList<>();

    static final int SMS_SEND_PERMISSION = 1;
    String authNum = "";
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        if (db == null) {
            db.execSQL("INSERT INTO USER VALUES ('" + " " + "');");
        }

        c = db.rawQuery("SELECT count(*) as count FROM GUEST", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            count = c.getString(0);
            c.moveToNext();
        }
        c.close();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        completeLogin = (Button) findViewById(R.id.completeLogin);
        goJoin = (Button) findViewById(R.id.goJoin);
        nonMember = (Button) findViewById(R.id.nonmember);

        goJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });

        nonMember.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent;
                db.execSQL("DELETE FROM USER;");
                if (count.equals("0")) {
                    intent = new Intent(LoginActivity.this, ChooseGuestAvatarActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, GuestHomeActivity.class);
                }
                startActivity(intent);
            }
        });

        LoginActivity.GetData task = new LoginActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/login.php");
    }

    public static String numberGen(int len, int dupCd) {

        Random rand = new Random();
        String numStr = "";  // 난수

        for(int i=0; i<len; i++) {
            //0~9까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));

            if(dupCd==1) {  //중복 허용시 numStr에 append
                numStr += ran;
            } else if(dupCd==2) { //중복 허용X
                if(!numStr.contains(ran)) {
                    numStr += ran;
                }else {
                    i -= 1;   //중복시 루틴 다시 실행
                }
            }
        }
        return numStr;
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

        Toast.makeText(getBaseContext(), "인증 메세지가 전송되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LoginActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "login";
        String TAG_M_EMAIL = "m_email";
        String TAG_PW = "password";
        String TAG_PHONE = "phone";
        String TAG_OX = "device_ox";
        String TAG_MA_EMAIL = "ma_email";
        String TAG_AVATAR = "avatar";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String mem_email = item.getString(TAG_M_EMAIL);
                String mem_pw = item.getString(TAG_PW);
                String mem_phone = item.getString(TAG_PHONE);
                String mem_device = item.getString(TAG_OX);
                String mem_email2 = item.getString(TAG_MA_EMAIL);
                String mem_avatar = item.getString(TAG_AVATAR);

                emailList.add(mem_email);
                passwordList.add(mem_pw);
                maList.add(mem_email2);
                oxList.add(mem_device);
                phoneList.add(mem_phone);

            }

            completeLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strE = email.getText().toString();
                    String strP = password.getText().toString();
                    Intent intent;
                    if((emailList.indexOf(strE) != -1) && (maList.indexOf(strE) != -1) && (passwordList.indexOf(strP) != -1) && (emailList.indexOf(strE) == passwordList.indexOf(strP))) {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("email", strE);
                        intent.putExtra("password", strP);
                        db.execSQL("DELETE FROM USER;");
                        db.execSQL("INSERT INTO USER VALUES ('" + strE + "');");
                        int index = emailList.indexOf(strE);
                        if(oxList.get(index).equals("yes")) {
                            startActivity(intent);
                        } else {
                            SharedPreferences pref = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            authNum = numberGen(6, 1);
                            editor.putString("authNum", authNum);
                            editor.apply();
                            String phoneNum = phoneList.get(index);
                            sendSMS(phoneNum, "[메타버스] 인증번호 [" + authNum + "]를 입력해주세요.");

                            AlertDialog.Builder input = new AlertDialog.Builder(LoginActivity.this);
                            dialog = input.setTitle("인증번호 입력 및 ")
                                    .setMessage("전송된 인증번호를 입력해주세요.")
                                    .setView(R.layout.dialog_login)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //Override
                                        }
                                    }).create();
                            dialog.show();;

                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    EditText checkNum = dialog.findViewById(R.id.checkNum);
                                    CheckBox yes = dialog.findViewById(R.id.yes);
                                    CheckBox no = dialog.findViewById(R.id.no);
                                    Boolean close = false;
                                    if(pref.getString("authNum", "").equals(checkNum.getText().toString())) {
                                        Toast.makeText(getApplicationContext(), "인증되었습니다.", Toast.LENGTH_LONG).show();
                                        if(yes.isChecked() && !no.isChecked()) {
                                            close = true;
                                            String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                                            updateMem(strE);
                                            insertDevice(strE, android_id);
                                            startActivity(intent);
                                        } else if (no.isChecked() && !yes.isChecked()) {
                                            close = true;
                                            startActivity(intent);
                                        } else if (yes.isChecked() && no.isChecked()) {
                                            Toast.makeText(getApplicationContext(), "기기 등록 여부를 하나만 선택해주세요.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "기기 등록 여부를 선택해주세요.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        close = false;
                                        Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                                    }
                                    if (close) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }

                    } else if (emailList.indexOf(strE) != -1 && (maList.indexOf(strE) == -1) && (passwordList.indexOf(strP) != -1) && (emailList.indexOf(strE) == passwordList.indexOf(strP))) {
                        intent = new Intent(LoginActivity.this, ChooseAvatarActivity.class);
                        db.execSQL("DELETE FROM USER;");
                        db.execSQL("INSERT INTO USER VALUES ('" + strE + "');");
                        int index = emailList.indexOf(strE);
                        if(oxList.get(index).equals("yes")) {
                            startActivity(intent);
                        } else {
                            SharedPreferences pref = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            authNum = numberGen(6, 1);
                            editor.putString("authNum", authNum);
                            editor.apply();
                            String phoneNum = phoneList.get(index);
                            sendSMS(phoneNum, "[메타버스] 인증번호 [" + authNum + "]를 입력해주세요.");

                            AlertDialog.Builder input = new AlertDialog.Builder(LoginActivity.this);
                            dialog = input.setTitle("인증번호 입력 및 ")
                                    .setMessage("전송된 인증번호를 입력해주세요.")
                                    .setView(R.layout.dialog_login)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //Override
                                        }
                                    }).create();
                            dialog.show();;

                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    EditText checkNum = dialog.findViewById(R.id.checkNum);
                                    CheckBox yes = dialog.findViewById(R.id.yes);
                                    CheckBox no = dialog.findViewById(R.id.no);
                                    Boolean close = false;
                                    if(pref.getString("authNum", "").equals(checkNum.getText().toString())) {
                                        Toast.makeText(getApplicationContext(), "인증되었습니다.", Toast.LENGTH_LONG).show();
                                        if(yes.isChecked() && !no.isChecked()) {
                                            close = true;
                                            String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                                            updateMem(strE);
                                            insertDevice(strE, android_id);
                                            startActivity(intent);
                                        } else if (no.isChecked() && !yes.isChecked()) {
                                            close = true;
                                            startActivity(intent);
                                        } else if (yes.isChecked() && no.isChecked()) {
                                            Toast.makeText(getApplicationContext(), "기기 등록 여부를 하나만 선택해주세요.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "기기 등록 여부를 선택해주세요.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        close = false;
                                        Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                                    }
                                    if (close) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "회원 정보가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    }

                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private void insertDevice (String e, String d) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Please Wait", null, true, true);
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
                    String dText = (String) params[1];

                    String link = "http://"+ IP_ADDRESS +"/insert_device.php";
                    String data = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(eText, "UTF-8");
                    data += "&" + URLEncoder.encode("dname", "UTF-8") + "=" + URLEncoder.encode(dText, "UTF-8");

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
        task.execute(e, d);
    }

    private void updateMem (String e) {
        class UpdateData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "Please Wait", null, true, true);
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

                    String link = "http://"+ IP_ADDRESS +"/update_member.php";
                    String data = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(eText, "UTF-8");

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
        UpdateData task = new UpdateData();
        task.execute(e);
    }

}
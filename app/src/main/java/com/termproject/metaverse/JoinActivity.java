package com.termproject.metaverse;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
import java.util.Random;

public class JoinActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "join";

    Button checkNick, checkEmail, checkEmailNum, checkPassword, check, join;
    EditText nickname, email, emailNum, password, againPassword, phone;
    Switch device_switch;
    TextView device_name;

    String emailAuth, diaMessage = "";
    boolean canJoin = false;
    private AlertDialog dialog;

    static final int SMS_SEND_PERMISSION = 1;
    String authNum = "";

    String jsonString;
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> emailList = new ArrayList<>();

    String [] info = new String[6]; //0:nickname, 1:email, 2:password, 3:phone, 4: device_ox, 5:device_name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //버튼
        checkNick = findViewById(R.id.checkNick);
        checkEmail = findViewById(R.id.checkEmail);
        checkEmailNum = findViewById(R.id.checkEmailNum);
        checkPassword = findViewById(R.id.checkPassword);
        check = findViewById(R.id.check);
        join = findViewById(R.id.join);

        //입력
        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        emailNum = findViewById(R.id.emailNum);
        password = findViewById(R.id.password);
        againPassword = findViewById(R.id.againPassword);
        phone = findViewById(R.id.phone);

        //스위치
        device_switch = findViewById(R.id.device_switch);

        //텍스트뷰
        device_name = findViewById(R.id.device_name);

        checkEmailNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmn = emailNum.getText().toString();
                String strE = email.getText().toString();
                if(strEmn.equals(emailAuth)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("인증되었습니다.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    info[1] = strE;
                    Log.d("email", info[1]);
                    canJoin = true;
                } else if(strEmn.equals("")) {
                    canJoin = false;

                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("인증코드를 입력해주세요.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                } else {
                    canJoin = false;

                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("인증코드가 일치하지 않습니다.\n다시 입력해주세요.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                }
            }
        });

        checkPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPW = password.getText().toString();
                String strPW2 = againPassword.getText().toString();
                if((!strPW.trim().isEmpty()) && (strPW.equals(strPW2))) {
                    info[2] = strPW;
                    Log.d("password", info[2]);
                    Toast.makeText(getApplicationContext(), "비밀번호 확인이 완료되었습니다.", Toast.LENGTH_LONG).show();
                } else if ((strPW.trim().isEmpty()) || (strPW2.trim().isEmpty())) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //문자 보내기 권한 거부
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
            //문자 보내기 권한 허용
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
        }

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strP = phone.getText().toString();
                if (!strP.trim().isEmpty()) {
                    authNum = numberGen(6, 1);

                    editor.putString("authNum", authNum);
                    editor.apply();
                    sendSMS(phone.getText().toString(), "[메타버스] 인증번호 [" + authNum + "]를 입력해주세요.");

                    AlertDialog.Builder input = new AlertDialog.Builder(JoinActivity.this);
                    final EditText checkNum = new EditText(JoinActivity.this);
                    dialog = input.setTitle("인증번호 입력")
                            .setMessage("전송된 인증번호를 입력해주세요.")
                            .setView(checkNum)
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
                            Boolean close = false;
                            if(pref.getString("authNum", "").equals(checkNum.getText().toString())) {
                                close = true;
                                info[3] = strP;
                                Log.d("phone", info[3]);
                                Toast.makeText(getApplicationContext(), "인증되었습니다.", Toast.LENGTH_LONG).show();
                            } else {
                                close = false;
                                Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                            }
                            if (close) {
                                dialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "휴대전화번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }

            }
        });

        device_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if(isChecked) {
                    info[4] = "yes";
                    Log.d("ox", info[4]);
                    String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                    device_name.setText(android_id);
                } else {
                    info[4] = "no";
                    Log.d("ox", info[4]);
                    device_name.setText(null);
                }
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strD = device_name.getText().toString();
                Intent intent;
                if((info[0] != null) && (info[1] != null) && (info[2] != null) && (info[3] != null)) {
                    if (!strD.trim().isEmpty()) {
                        info[5] = strD;
                        Log.d("device",info[5]);
                        insertMem(info[0], info[1], info[2], info[3], info[4]);
                        insertDevice(info[1], info[5]);
                    } else {
                        info[4] ="no";
                        insertMem(info[0], info[1], info[2], info[3], info[4]);
                    }
                    intent = new Intent(JoinActivity.this, LoginActivity.class);
                    Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "회원 정보를 모두 입력해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        JoinActivity.GetData task = new JoinActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/member.php");
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
        //PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
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

            progressDialog = ProgressDialog.show(JoinActivity.this, "Please Wait", null, true, true);
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

        String TAG_JSON = "member";
        String TAG_NAME = "nickname";
        String TAG_EMAIL = "email";
        String TAG_PW = "password";
        String TAG_PHONE = "phone";
        String TAG_OX = "device_ox";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                String mem_name = item.getString(TAG_NAME);
                String mem_email = item.getString(TAG_EMAIL);
                String mem_pw = item.getString(TAG_PW);
                String mem_phone = item.getString(TAG_PHONE);
                String mem_device = item.getString(TAG_OX);

                nameList.add(mem_name);
                emailList.add(mem_email);
            }

            checkNick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strN = nickname.getText().toString();
                    if((nameList.indexOf(strN) == -1) && (!strN.trim().isEmpty())) {
                        info[0] = strN;
                        Log.d("name ", info[0]);
                        Toast.makeText(getApplicationContext(), "사용 가능한 닉네임입니다.", Toast.LENGTH_LONG).show();
                    } else if (strN.trim().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "사용중인 닉네임입니다. 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            checkEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strE = email.getText().toString();
                    if((emailList.indexOf(strE) == -1) && (!strE.trim().isEmpty())) {
                        new Thread() {
                            public void run() {
                                NMailSender nMailSender = new NMailSender();
                                nMailSender.sendMail(strE);
                                emailAuth = nMailSender.emailCode;  // 인증코드 가져오기
                                diaMessage = nMailSender.diaMessage;
                            }
                        }.start();
                        Toast.makeText(getApplicationContext(), "사용 가능한 이메일입니다. 메일에서 인증 코드를 확인해주세요.", Toast.LENGTH_LONG).show();
                    } else if (strE.trim().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "사용중인 이메일입니다. 다시 입력해주세요.", Toast.LENGTH_LONG).show();
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
                loading = ProgressDialog.show(JoinActivity.this, "Please Wait", null, true, true);
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

    private void insertMem (String n, String e, String pw, String p, String ox) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(JoinActivity.this, "Please Wait", null, true, true);
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
                    String eText = (String) params[1];
                    String pwText = (String) params[2];
                    String pText = (String) params[3];
                    String oxText = (String) params[4];

                    String link = "http://"+ IP_ADDRESS +"/insert_member.php";
                    String data = URLEncoder.encode("nickname","UTF-8") + "=" + URLEncoder.encode(nText, "UTF-8");
                    data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(eText, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pwText, "UTF-8");
                    data += "&" + URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(pText, "UTF-8");
                    data += "&" + URLEncoder.encode("device_ox", "UTF-8") + "=" + URLEncoder.encode(oxText, "UTF-8");

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
        task.execute(n, e, pw, p, ox);
    }
}
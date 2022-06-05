package com.termproject.metaverse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class FragmentSetting extends Fragment {

    TextView logout, out;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;
    String strId;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "setting";

    private AlertDialog dialog;

    public FragmentSetting() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container,false);

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        c = db.rawQuery("SELECT * FROM USER", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            strId = c.getString(0);
            c.moveToNext();
        }
        c.close();

        logout = (TextView) view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                db.execSQL("DELETE FROM USER;");
                startActivity(intent);
            }
        });

        out = (TextView) view.findViewById(R.id.out);
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                dialog = builder.setMessage("탈퇴하시겠습니까? \n탈퇴 시 모든 회원 정보가 삭제됩니다.")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.execSQL("DELETE FROM USER;");
                                deleteDevice(strId);
                                deleteFriend(strId);
                                deleteMA(strId);
                                deleteMem(strId);
                                startActivity(intent);
                            }
                        }).setNeutralButton("아니오", null)
                        .create();
                dialog.show();
            }
        });

        return view;
    }

    private void deleteMA (String e) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
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

                    String link = "http://"+ IP_ADDRESS +"/out_ma.php";
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
        DeleteData task = new DeleteData();
        task.execute(e);
    }

    private void deleteMem (String e) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
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

                    String link = "http://"+ IP_ADDRESS +"/out_member.php";
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
        DeleteData task = new DeleteData();
        task.execute(e);
    }

    private void deleteDevice (String e) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
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

                    String link = "http://"+ IP_ADDRESS +"/out_device.php";
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
        DeleteData task = new DeleteData();
        task.execute(e);
    }

    private void deleteFriend (String e) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
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

                    String link = "http://"+ IP_ADDRESS +"/out_friend.php";
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
        DeleteData task = new DeleteData();
        task.execute(e);
    }

}

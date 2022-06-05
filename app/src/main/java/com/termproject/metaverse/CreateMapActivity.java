package com.termproject.metaverse;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class CreateMapActivity extends AppCompatActivity {

    ImageView newBackground;
    Button addBackground;

    private static String IP_ADDRESS = "000.000.000.000"; //IP_ADDRESS 변경 필요
    private static String TAG = "map";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createmap);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        newBackground = (ImageView) findViewById(R.id.newBackground);

        addBackground = (Button) findViewById(R.id.addBackground);
        addBackground.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        Button completeCreate = (Button) findViewById(R.id.completeCreate);
        completeCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateMapActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String imgName = getImageName(uri);
                insertMap(imgName, "personal");
                ContentResolver resolver = getContentResolver();
                try {
                    InputStream input = resolver.openInputStream(uri);
                    Bitmap imgBitmap = BitmapFactory.decodeStream(input);
                    newBackground.setImageBitmap(imgBitmap);
                    input.close();
                    saveBitmapToPNG(imgBitmap, imgName);
                } catch (Exception e) {
                    Log.d("Tag", "background : ", e);
                }
            }
        }
    }

    public String getImageName(Uri data) {
        String[] img = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, img, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }

    public void saveBitmapToPNG(Bitmap bitmap, String name) {
        File file = new File(getCacheDir(), name);
        try {
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
        } catch (Exception e) {
            Log.d("Tag", "bitmap to png : ", e);
        }
    }

    private void insertMap (String mn, String ac) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(CreateMapActivity.this, "Please Wait", null, true, true);
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
                    String mText = (String) params[0];
                    String aText = (String) params[1];

                    String link = "http://"+ IP_ADDRESS +"/insert_map.php";
                    String data = URLEncoder.encode("mname","UTF-8") + "=" + URLEncoder.encode(mText, "UTF-8");
                    data += "&" + URLEncoder.encode("access", "UTF-8") + "=" + URLEncoder.encode(aText, "UTF-8");

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
        task.execute(mn, ac);
    }
}

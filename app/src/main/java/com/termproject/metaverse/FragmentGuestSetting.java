package com.termproject.metaverse;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentGuestSetting extends Fragment {

    TextView init;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor c;

    public FragmentGuestSetting() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guestsetting, container,false);

        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        init = (TextView) view.findViewById(R.id.init);
        init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                db.execSQL("DELETE FROM GUEST;");
                startActivity(intent);
            }
        });

        return view;
    }
}

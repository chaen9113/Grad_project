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


public class FragmentGuestFriend extends Fragment {

    TextView warning;

    public FragmentGuestFriend() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guestfriend, container,false);

        warning = (TextView)  view.findViewById(R.id.warning);

        String warn = "비회원은 친구 기능을 사용할 수 없습니다. 친구 기능을 사용하려면 로그인 후 이용해주세요.";
        warning.setText(warn);

        return view;
    }


}

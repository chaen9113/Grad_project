package com.termproject.metaverse;

import android.graphics.drawable.Drawable;

public class ListItem {

    private Drawable img;
    private String txt1;

    public String getTxt1() {
        return this.txt1;
    }

    public void setTxt1(String t1) {
        this.txt1 = t1;
    }

    public Drawable getImg() {
        return this.img;
    }

    public void setImg(Drawable i) {
        this.img = i;
    }
}

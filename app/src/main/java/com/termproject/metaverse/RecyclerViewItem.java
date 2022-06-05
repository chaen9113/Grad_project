package com.termproject.metaverse;

import android.graphics.drawable.Drawable;

public class RecyclerViewItem {
    private Drawable drawableIcon;

    public void setIcon(Drawable icon) {
        drawableIcon = icon;
    }

    public Drawable getIcon() {
        return this.drawableIcon;
    }

}

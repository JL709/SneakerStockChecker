package com.sneaks.sneakerstockchecker;

import android.graphics.Bitmap;

/**
 * Created by Jimmy on 2017-05-23.
 */

public class ShoeInfo {
    String name;
    String pid;
    Bitmap image;

    public ShoeInfo(String name, String pid, Bitmap image) {
        this.name = name;
        this.pid = pid;
        this.image = image;
    }
}

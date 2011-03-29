/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.andromiscid;

import android.graphics.Bitmap;

/**
 *
 * @author twilight
 */
public interface BitmapSourceListener {

    public void stopped();

    public void imageReceived(Bitmap bmp);

}

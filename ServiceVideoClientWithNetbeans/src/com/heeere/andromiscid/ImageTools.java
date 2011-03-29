/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.andromiscid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import fr.prima.omiscid.user.connector.Message;

/**
 *
 * @author twilight
 */
public class ImageTools {

    static Bitmap createBufferedImageFromRawMessage(Message message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static Bitmap createBufferedImageFromJpegMessage(Message message) {
        byte[] data = message.getBuffer();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

}

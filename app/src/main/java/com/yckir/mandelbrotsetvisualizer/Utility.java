package com.yckir.mandelbrotsetvisualizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utility {

    /**
     * Logs various details about a file.
     *
     * @param file the file that will be logged
     */
    public static void logFileDetails(File file){
        Log.v("FILE", "file name is = " + file.getName());
        Log.v("FILE", "is directory == " + file.isDirectory());
        Log.v("FILE", "is file == " + file.isFile());
        Log.v("FILE", "free space is = " + file.getFreeSpace());
        Log.v("FILE", "path is = " + file.getPath());
        Log.v("FILE", "parent is = " + file.getParent());
        Log.v("FILE", "absolute path is = " + file.getAbsolutePath());

        if(file.exists())
            Log.v("FILE", "file exists");
        else
            Log.v("FILE", "file doesn't exists");
    }


    /**
     * Writes a bitmap to a PNG file with 90% quality.
     *
     * @param bitmap the bitmap of the image
     * @param file the file to write to
     * @return true if the write was successful.
     */
    public static boolean writeBitmapToPNG(Bitmap bitmap, File file){
        Log.v("FILE", "writing image file " + file.getName());
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Gets a bitmap from the file location.
     *
     * @param file the file location
     * @return the bitmap of a file, null if bitmap couldn't be decoded.
     */
    public static Bitmap getFileBitmap(File file){
        Log.v("FILE", "reading image file " + file.getName());
        return BitmapFactory.decodeFile(file.getPath());
    }
}

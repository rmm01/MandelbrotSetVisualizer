package com.yckir.mandelbrotsetvisualizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utility {
    private static final String EXTERNAL_DIR_NAME = "MandelbrotVisualizer";

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


    /**
     * Check to see if the external storage can be written to. This checks to see if external
     * storage is currently mounted.
     *
     * @return true if the external storage can be written to.
     */
    public static boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Gets the File for the apps External directory where all images will be saved to. If this
     * directory does not exits, then this method is being called for first time since app was
     * installed and the directory will be created and returned.
     *
     * @return the apps External directory where all images should be saved to.
     */
    public static File getAppExternalDirectory(){
        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File( external , EXTERNAL_DIR_NAME);
        if(file.exists())
           return file;

        if(!file.mkdirs()){
            Log.e("FILE","Directory could not be made");
        }
        return file;
    }

}

package com.yckir.mandelbrotsetvisualizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Contains utility methods and fields for file related operations.
 */
public class Utility {
    public static final String EXTERNAL_PIC_DIR = "MandelbrotVisualizer/Pictures";
    public static final String EXTERNAL_ANIMATION_DIR = "MandelbrotVisualizer/Animation";
    public static final String INTERNAL_NAV_DIR = "Navigation";
    public static final String ANIMATION_DETAILS_FILE = "info.txt";
    public static final long IMAGE_SIZE = 200000;//200 kilobytes

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
     * Makes all directories that the app should save to.
     *
     * @param context context of the app
     * @return true if all directories exist, false if one or more could not be created
     */
    public static boolean makeStorageDirectories(Context context){
        boolean result = true;
        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File externalPic = new File( external , EXTERNAL_PIC_DIR);
        if(!externalPic.exists())
            if(!externalPic.mkdirs())
                result = false;

        File externalVid = new File(external, EXTERNAL_ANIMATION_DIR);
        if(!externalVid.exists())
            if(!externalVid.mkdirs())
                result = false;

        File internalNav =new File( context.getFilesDir(), INTERNAL_NAV_DIR );
        if(!internalNav.exists())
            if(!internalNav.mkdirs())
                result = false;

        return result;
    }


    /**
     * Gets the File for the apps External directory where all images will be saved to.
     *
     * @return the apps External directory where all images should be saved to.
     */
    public static File getExternalPictureDir(){
        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File( external , EXTERNAL_PIC_DIR);

    }


    /**
     * Gets the File for the apps External directory where all animation images will be saved to.
     *
     * @return the apps External directory where all animation images should be saved to.
     */
    public static File getExternalAnimationDir(){
        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File( external, EXTERNAL_ANIMATION_DIR);
    }


    /**
     * Gets the File for the apps Internal directory where all navigation images will be saved to.
     *
     * @return the apps External directory where all navigation images should be saved to.
     */
    public static File getInternalNavigationDir(Context context){
        return new File(context.getFilesDir(), INTERNAL_NAV_DIR);
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
     * Check to see if at least 200 kilobytes exist in the file path.
     *
     * @param file file location
     * @return true if 200 kilobytes exist in the file path.
     */
    public static boolean imageSpaceExists(File file){
        if(file.getFreeSpace()<IMAGE_SIZE){
            Log.e("FILE", "Free space is less than 200 kilobytes, remaining space is " + file.getFreeSpace());
            return false;
        }
        return true;
    }


    /**
     * Send a media broadcast to the given file.
     *
     * @param file file location sent to media broadcast.
     * @param context context of the app.
     */
    public static void sentMediaStoreBroadcast(File file, Context context){
        //send broadcast to make image visible to gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    /**
     * Reads a file and returns the contents.
     *
     * @param directory the file location
     * @return the contents of the file in string format.
     */
    public static String readFile(File directory){
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(directory,ANIMATION_DETAILS_FILE));
            InputStreamReader reader= new InputStreamReader(inputStream);
            char[] charBuffer= new char[100];
            String contents="";
            int charactersRead;
            while( ( charactersRead = reader.read(charBuffer)) > 0 ){
                String textPortion = String.copyValueOf(charBuffer,0,charactersRead);
                contents += textPortion;
            }
            inputStream.close();
            Log.v("FILE", "reading contents are[" + contents + "]");
            return contents;
        } catch (FileNotFoundException e) {
            Log.v("FILE", "file not found");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.v("FILE","IO exception");
            e.printStackTrace();
            return null;
        }
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Write a file with the details of an animation.
     *
     * @param delay how long to delay frames of animation.
     * @param lastFrameNum the last frame number of the animation. Assumed that first frame is zero.
     * @param directory the location to save the file.
     */
    public static void writeAnimationDetails(int delay, int lastFrameNum, File directory){
        String content = Integer.toString(delay) + "\n" + Integer.toString(lastFrameNum);
        Log.v("FILE", "writing contents are[" + content + "]");
        File file = new File(directory,ANIMATION_DETAILS_FILE);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Splits the string containing the animation files contents.
     *
     * @param content context of the app
     * @return the content of the animation file in string format
     */
    public static String[] splitAnimationFileDetails(String content){
        return content != null ? content.split("\n") : new String[0];
    }


    /**
     * Gets the delay of the animation file from the contents of splitAnimationFileDetails.
     *
     * @param contents file containing animation details. Should always be splitAnimationFileDetails
     * @return the delay of the animation.
     */
    public static int getAnimationFileDelay(String[] contents){
        return Integer.parseInt(contents[0]);
    }


    /**
     * Gets the last frame number of the animation file from the contents of splitAnimationFileDetails.
     *
     * @param contents file containing animation details. Should always be splitAnimationFileDetails
     * @return the last frame number of the animation.
     */
    public static int getAnimationFileLastFrame(String[] contents){
        return Integer.parseInt(contents[1]);
    }
}

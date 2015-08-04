package com.example.tjmelanson.buddycam_httpvid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjmelanson on 8/2/15.
 */
public class ImageCaptureThread extends Thread {

    private ImageTask mTask;
    //To measure the progress of the download (for funsies)
    public static final int CONNECTION_SETUP = 0;
    public static final int ISTREAM_CAPTURED = 1;
    public static final int BITMAP_COMPLETED = 2;

    private static final byte[] JPEG_START = {(byte) 0xff, (byte) 0xd8};
    private static final byte[] JPEG_END =   {(byte) 0xff, (byte) 0xd9};
    private String vidAddress = "http://130.108.213.245:8081"; //"http://130.108.213.245:8081";

    private byte[] imageBytes;
    private InputStream is;

    ImageCaptureThread(ImageTask imageTask) {
        mTask = imageTask;
    }


    //Gets an inputstream from the URL
    public InputStream getStreamFromURL(String vidAddress){
        try{
            URL url = new URL(vidAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();

            return is;
        }  catch (MalformedURLException e) {
            Log.e(null, "Malformed URL");
        } catch (IOException e) {
            Log.e(null, "IOException");
        }
        return null;
    }

    //Finds the index of a key in the array (Java doesn't support this as far as I know)
    private int findIndex(byte[] buffer, byte[] key, int length) {

        int keyLen = key.length;
        for (int i=0; i<=length-keyLen; i++){
            boolean isEqual=true;
            for (int j=0; j<keyLen;j++) {
                if (key[j] != buffer[i+j]){
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) return i;
        }
        return -1;
    }

    private void transferBytes(byte[] buffer, ArrayList<Byte> destination, int length){
        for (int i=0; i<length; i++)
            destination.add(buffer[i]);
    }

    //Reads until a specified key is reached.
    //Can be used to find the starting point and ending point of a JPEG
    private int readUntilKey(InputStream is, ArrayList<Byte> list, byte[] key, int startingPoint) throws IOException{
        int totalLength=0, readLength, index; //length is the length of a single byte read

         do{
            byte[] buffer = new byte[2048];
            readLength = is.read(buffer);
            index = findIndex(buffer, key, readLength);
            //Transfers bytes from buffer to the current list
            transferBytes(buffer, list, readLength);
            totalLength += readLength;
        } while (index == -1 && readLength > 0);

        if (index == -1) return -1;

        //Why dis so long?? Needs total length, minus total bytes in current read, plus the previous starting point of read,
        //plus the relative index of the byte key in the read
        return totalLength - readLength + startingPoint + index;
    }

    // Runs the code for this task
    public void run() {
        // Retrieves the stream
        InputStream is = getStreamFromURL(vidAddress);

        //Runs continuously
        while (true){


            ArrayList<Byte> byteList = new ArrayList<Byte>();
            int start=0, end=0;


            try{
                start = readUntilKey(is, byteList, JPEG_START, 0);

                //Log.i(null, "Value of byteList:");
                //Log.i(null, String.valueOf(byteList.toString()));

                end = readUntilKey(is, byteList, JPEG_END, byteList.size());
            } catch (IOException e) {
                Log.e(null, "Exception: Can't read from stream");
                break;
            } catch (NullPointerException e){
                //Turn this into a handler message, so we can make toasts from it (yum!)
                Log.e(null, "Error: Stream not available");
                break;
            }

            int imageSize = byteList.size();
            imageBytes = new byte[imageSize];
            for (int i = 0; i<imageSize; i++) {
                imageBytes[i] = byteList.get(i);
            }

            //Log.e(null, String.valueOf(imageBytes[start]));
            //Log.e(null, String.valueOf(imageBytes[end]));

            //Decodes it
            Bitmap returnBitmap = BitmapFactory.decodeByteArray(imageBytes, start, end+2 - start);

            // Sets the ImageView Bitmap
            mTask.setImage(returnBitmap);
            // Reports a status of "completed"
            mTask.handleRetrieveState(BITMAP_COMPLETED);
        }

    }

}

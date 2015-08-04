package com.example.tjmelanson.buddycam_httpvid;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by tjmelanson on 8/2/15.
 *
 * This class is the general task manager. Any OpenCV calls, as well as email calls, will be done via this device.
 */
public class ImageTask {

    public static final int IN_PROGRESS=0;
    public static final int COMPLETE=1;

    public static int image_state;

    private Bitmap img;
    private Handler mHandler;

    public ImageTask(Handler h){
        mHandler = h;
    }

    public void setImage(Bitmap returnBitmap) {
        img = returnBitmap;
    }

    public Bitmap getImage(){
        return img;
    }

    public void handleRetrieveState(int bitmap_prog) {
        int outState;
        switch (bitmap_prog){
            case ImageCaptureThread.BITMAP_COMPLETED:{
                image_state = COMPLETE;
                //Log.i(null, "Image downloaded");
            }   break;
            default: image_state = IN_PROGRESS; break;
        }
        handleState();
    }

    public void handleState(){
        /*
         * Passes a handle to this task and the
         * current state to the class that created
         * the thread pools
         */
        switch (image_state) {

            // The task finished downloading and decoding the image
            case COMPLETE:
                /*
                 * Creates a message for the Handler
                 * with the state and the task object
                 */
                Message completeMessage =
                        mHandler.obtainMessage(image_state, img);
                completeMessage.sendToTarget();
                break;

        }

    }
}

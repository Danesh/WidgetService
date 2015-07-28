// DoodleView.java
// Main View for the Doodlz app
package com.example.danesh.surfaceservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

// the main screen that is painted
public class DoodleView extends View
{
    // used to determine whether user moved a finger enough to draw again
    private static final float TOUCH_TOLERANCE = 10;

    // create SimpleOnGestureListener for single tap events
    private final GestureDetector.OnGestureListener mSingleTapListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if((getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
                hideSystemBars();
            else
                showSystemBars();

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }
    };

    private Bitmap mBitmap; // drawing area for display or saving
    private Canvas mBitmapCanvas;   // used to draw on bitmap
    private final Paint mPaintScreen;   // used to draw bitmap onto screen
    private final Paint mPaintLine;   // used to draw lines onto bitmap

    // Maps of current Paths being drawn and Points in those Paths
    private final Map<Integer, Path> mPathMap = new HashMap<Integer, Path>();
    private final Map<Integer, Point> mPreviousPointMap = new HashMap<Integer, Point>();

    // used to hide/show system bars
    private GestureDetector mSingleTapDetector;

    public DoodleView(Context context) {
        super(context);  // pass the context to View's constructor
        mPaintScreen = new Paint(); // used to display bitmap onto screen

        // set the initial settings for the painted line
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);  // smooth edges of drawn line
        mPaintLine.setColor(Color.BLACK);   // default color is black
        mPaintLine.setStyle(Paint.Style.STROKE);    // solid line
        mPaintLine.setStrokeWidth(5);   // set the default width
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);   // rounded line ends

        // GestureDetector for single taps
        mSingleTapDetector = new GestureDetector(getContext(), mSingleTapListener);
    }

    // clear the painting
    public void clear() {
        mPathMap.clear();   // remove all paths
        mPreviousPointMap.clear();  // remove all previous points
        mBitmap.eraseColor(Color.WHITE);    // clear the bitmap
        invalidate();   // refresh the screen
    }

    // set the painted line's color
    public void setDrawingColor(int color) {
        mPaintLine.setColor(color);
    }

    // return the painted line's color
    public int getDrawingColor() {
        return mPaintLine.getColor();
    }

    // set the painted line's width
    public void setLineWidth(int width) {
        mPaintLine.setStrokeWidth(width);
    }

    // return the painted line's width
    public int getLineWidth() {
        return (int)mPaintLine.getStrokeWidth();
    }

    // hide system bars and action bar
    public void hideSystemBars() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE

            );
    }

    // show system bars and action bar
    public void showSystemBars() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
    }

    // save the current image to the Gallery
    public void saveImage() {
        // use "Doodlz" followed by current time as the image name
        String name = "Doodlz" + System.currentTimeMillis() + ".jpg";

        // insert the image in the device's gallery
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), mBitmap, name, "Doodlz Drawing"
        );

        if(location != null)    // image was saved
        {
            // displayed a message indicating that the image was saved
//            Toast message = Toast.makeText(getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
//            message.setGravity(Gravity.CENTER, message.getXOffset()/2, message.getYOffset()/2);
//            message.show();
        }
        else
        {
            // display a message indicating that the image was saved
//            Toast message = Toast.makeText(getContext(), R.string.message_error_saving, Toast.LENGTH_SHORT);
//            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
//            message.show();
        }
    }

    // called when the use touches the screen
    private void touchStarted(float x, float y, int lineId) {
        Path path;  // used to store the path for the given touch id
        Point point; // used to store the last point in path

        // if there is already a path for lineID
        if(mPathMap.containsKey(lineId)) {
            path = mPathMap.get(lineId);    // get the Path
            path.reset();   // reset the Path because a new touch has started
            point = mPreviousPointMap.get(lineId);  // get Path's last point
        }
        else {
            path = new Path();
            mPathMap.put(lineId, path); // add the Path to Map
            point = new Point();    // create a new Point
            mPreviousPointMap.put(lineId, point);   // add the Point to the Map
        }

        // move to the coordinates of the touch
        path.moveTo(x, y);
        point.x = (int)x;
        point.y = (int)y;
    }

    // called when the user drags along the screen
    private void touchedMoved(MotionEvent event) {
        // for each of the pointers in the given MotionEvent
        for (int i = 0; i < event.getPointerCount(); i++) {
            // get the pointer ID and pointer index
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            // if there is a path associated with the pointer
            if(mPathMap.containsKey(pointerId)) {
                // get the new coordinates for the pointer
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                // get the Path and previous Point associated with
                // this pointer
                Path path = mPathMap.get(pointerId);
                Point point = mPreviousPointMap.get(pointerId);

                // calculate how far the user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                // if the distance is significant enough to matter
                if(deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    // move the path to the new location
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    // store the new coordinates
                    point.x = (int)newX;
                    point.y = (int)newY;
                }
            }
        }
    }

    // called when the user finishes a touch
    private void touchEnded(int lineId) {
        Path path = mPathMap.get(lineId);   // get the corresponding Path
        mBitmapCanvas.drawPath(path, mPaintLine);   // draw to bitmapCanvas
        path.reset();   // reset the Path
    }

    // called each time this View is drawn
    @Override
    protected void onDraw(Canvas canvas) {
        // draw the background screen
        canvas.drawBitmap(mBitmap, 0, 0, mPaintScreen);

        // for each path currently being drawn
        for (Integer key : mPathMap.keySet())
            canvas.drawPath(mPathMap.get(key), mPaintLine); // draw line
    }

    // Method onSizeChanged creates Bitmap and Canvas after app displays
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        mBitmapCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(Color.WHITE);    // erase the Bitmap with white
    }

    // handle touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get the event type and the ID of the pointer that caused the event
        // if a single tap event occurred on KitKat or higher device
        if(mSingleTapDetector.onTouchEvent(event))
            return true;

        int action = event.getActionMasked();  // event type
        int actionIndex = event.getActionIndex();   // pointer (i.e., finger)

        // determine whether touch started, ended or is moving
        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchedMoved(event);
        }

        invalidate();   // redraw
        return true;
    }
}
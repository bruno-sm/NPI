package com.example.bruno.museomatematico;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

public class MultiTouchViewPager extends ViewPager {
    private float mLastXPosition, mLastYPosition;
    private boolean scaleMove;
    private double lastdist;
    private SparseArray mActivePointers = new SparseArray();
    private OnTouchListener onTouchListener;

    public MultiTouchViewPager(Context context) {
        super(context);
        mLastXPosition = 0f;
        mLastYPosition = 0f;
        onTouchListener = new MultiTouchViewPager.OnTouchListener(){

            @Override
            public void onTouch() {
                Log.d("tl", "Touch");
            }

            @Override
            public void onRelease() {
                Log.d("tl", "Release");
            }

            @Override
            public void onPinchIn() {
                Log.d("tl", "PinchIn");
            }

            @Override
            public void onPinchOut() {
                Log.d("tl", "PinchOut");
            }

            @Override
            public void onMove() {
                Log.d("tl", "Move");
            }
        };
    }


    public MultiTouchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLastXPosition = 0f;
        mLastYPosition = 0f;
        onTouchListener = new MultiTouchViewPager.OnTouchListener(){

            @Override
            public void onTouch() {
                Log.d("tl", "Touch");
            }

            @Override
            public void onRelease() {
                Log.d("tl", "Release");
            }

            @Override
            public void onPinchIn() {
                Log.d("tl", "PinchIn");
            }

            @Override
            public void onPinchOut() {
                Log.d("tl", "PinchOut");
            }

            @Override
            public void onMove() {
                Log.d("tl", "Move");
            }
        };
    }


    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                mLastYPosition = event.getY();
                mLastXPosition = event.getX();
                onTouchListener.onTouch();
                return super.onTouchEvent(event);

            case MotionEvent.ACTION_UP:
                scaleMove = false;
                lastdist = 0;
                onTouchListener.onRelease();
                return super.onTouchEvent(event);

            case MotionEvent.ACTION_MOVE:
                int diffY = (int) (event.getY() - mLastYPosition);
                int diffX = (int) (event.getX() - mLastXPosition);

                mLastYPosition = event.getY();
                mLastXPosition = event.getX();

                //Check if the action was jitter
                if (Math.abs(diffX) > 4 || Math.abs(diffY) > 4) {

                    if (scaleMove) {
                        double dist = 0;
                        boolean res = false;

                        if (event.getPointerCount() >= 2) {
                            dist = Math.sqrt(Math.pow(event.getX(0) - event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));
                        }

                        if ((Math.abs(dist - lastdist) > 10) && (lastdist > 0) && (dist > 0)) {
                            if (dist < lastdist) {
                                onTouchListener.onPinchIn();
                            } else if (dist > lastdist) {
                                onTouchListener.onPinchOut();
                            }
                        } else {
                            res = super.onTouchEvent(event);
                        }

                        lastdist = dist;
                        return res;
                    } else {
                        onTouchListener.onMove();
                        return true;
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL: {
                scaleMove = false;
                mActivePointers.remove(pointerId);
                onTouchListener.onRelease();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                scaleMove = true;
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                mActivePointers.put(pointerId, f);

                return super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    public interface OnTouchListener {
        void onTouch();

        void onRelease();

        void onPinchIn();

        void onPinchOut();

        void onMove();
    }
}

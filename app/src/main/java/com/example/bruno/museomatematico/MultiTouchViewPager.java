package com.example.bruno.museomatematico;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MultiTouchViewPager extends ViewPager {

    public MultiTouchViewPager(Context context) {
        super(context);
    }


    public MultiTouchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.i("h", "AntonioCheca " + event.getPointerCount());
        //He intentado hacerlo por getPointerCount pero siempre devuelve 1 ??????
        //He intentado mirar a ver si puedo llamar a onInterceptTouch dentro del FrameLayout pero me
        //lo hacia con parones y no funcionaba bien
        if(event.getPointerCount() >= 2)
            super.onInterceptTouchEvent(event);
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}

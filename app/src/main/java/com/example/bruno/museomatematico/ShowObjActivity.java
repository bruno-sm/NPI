package com.example.bruno.museomatematico;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.rajawali3d.view.SurfaceView;

import java.io.Serializable;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShowObjActivity extends FragmentActivity {
    private static final int NUM_PAGES = 5;
    private MultiTouchViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private class ObjectViewerPageAdapter extends FragmentStatePagerAdapter {
        private Activity mActivity;


        public ObjectViewerPageAdapter(FragmentManager fm, Activity activity) {
            super(fm);
            mActivity = activity;
        }

        @Override
        public Fragment getItem(int postion) {
            Log.d("d", "Creando fragment");
            ObjectViewerFragment fragment = new ObjectViewerFragment();
            return fragment;
        }


        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_obj);
        mVisible = true;

        mPager = (MultiTouchViewPager) findViewById(R.id.obj_view_pager);
        mPagerAdapter = new ObjectViewerPageAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);

        TextView obj_text_view = (TextView) findViewById(R.id.obj_text_view);
        obj_text_view.setMovementMethod(new ScrollingMovementMethod());

        final TouchableFrameLayout frame = (TouchableFrameLayout) findViewById(R.id.touchable_frame);
        frame.setTouchListener(new TouchableFrameLayout.OnTouchListener() {
            @Override
            public void onTouch() {
                Log.i("h", "AntonioCheca onTouch");
            }

            @Override
            public void onRelease() {
                Log.i("h", "AntonioCheca onRelease");

            }

            @Override
            public void onPinchIn() {

                Log.i("h", "AntonioCheca onPinchIn");
            }

            @Override
            public void onPinchOut() {

                Log.i("h", "AntonioCheca onPinchOut");
            }

            @Override
            public void onMove() {
                Log.i("h", "AntonioCheca onMove");
            }

            @Override
            public void onTwoFingersDrag() {

                Log.i("h", "AntonioCheca onTwoFingersDrag");
            }

            @Override
            public void onSecondFingerOnLayout() {
                Log.i("h", "AntonioCheca onSecond");

            }

        });
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}

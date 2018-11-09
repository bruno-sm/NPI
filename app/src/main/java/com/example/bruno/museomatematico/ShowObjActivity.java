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
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.rajawali3d.view.SurfaceView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShowObjActivity extends FragmentActivity {
    private MultiTouchViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ArrayList<ObjInformation> mObjsInfo;
    int mCurrentObject;

    private class ObjectViewerPageAdapter extends FragmentStatePagerAdapter {
        private Activity mActivity;


        public ObjectViewerPageAdapter(FragmentManager fm, Activity activity) {
            super(fm);
            mActivity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("d", "Creando fragment");
            ObjectViewerFragment fragment = ObjectViewerFragment.newInstance(mObjsInfo.get(position).getType());
            return fragment;
        }


        @Override
        public int getCount() {
            return mObjsInfo.size();
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

        // Obtenemos los objetos que mostrará esta activity
        mObjsInfo = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        int objTypes[] = extras.getIntArray("com.example.museomatematico.ObjTypes");
        for(int i: objTypes) {
            mObjsInfo.add(new ObjInformation(ObjInformation.ObjType.from(i)));
        }
        mCurrentObject = 0;

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_obj);
        mVisible = true;

        TextView obj_text_view = (TextView) findViewById(R.id.obj_text_view);
        obj_text_view.setMovementMethod(new ScrollingMovementMethod());
        setDescriptionText();

        mPager = (MultiTouchViewPager) findViewById(R.id.obj_view_pager);
        mPagerAdapter = new ObjectViewerPageAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mCurrentObject = i;
                setDescriptionText();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        TouchableFrameLayout frame = (TouchableFrameLayout) findViewById(R.id.touchable_frame);
        frame.setTouchListener(new TouchableFrameLayout.OnTouchListener() {
            @Override
            public void onTouch() {

            }

            @Override
            public void onRelease() {

            }

            @Override
            public void onPinchIn() {

            }

            @Override
            public void onPinchOut() {

            }

            @Override
            public void onMove() {

            }

            @Override
            public void onTwoFingersDrag() {

            }

            @Override
            public void onSecondFingerOnLayout() {

            }

        });
    }


    private void setDescriptionText() {
        TextView objTextView = (TextView) findViewById(R.id.obj_text_view);
        ObjInformation objInfo = mObjsInfo.get(mCurrentObject);
        HashMap<String, String> properties = objInfo.getProperties();

        String text = "<big>" + objInfo.getName() + "</big>\n";
        for (String key : properties.keySet()) {
            text = text + "<b>" + key + "<b>: " + properties.get(key) + "\n\n";
        }

        objTextView.setText(Html.fromHtml(text));
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

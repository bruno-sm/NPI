package com.example.bruno.museomatematico;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.rajawali3d.view.SurfaceView;


public class ObjectViewerFragment extends Fragment {
    private SurfaceView mRajawaliSurface;
    private ObjInformation mObjInfo;
    public boolean callGetOnTouchListener = false;
    private int mId;
    private boolean cam_light;
    private float l;
    private boolean alreadyAsked;
    private ObjRenderer renderer;

    private MyCameraManager my_camera;
    private int req_code_camera = 2;

    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode;

    {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                my_camera.initCamera(getActivity(), alreadyAsked, renderer);
                alreadyAsked = true;
                //my_camera.initCamera(ShowObjActivity.this);
                Log.d("Handlers", "Called on main thread");
                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(runnableCode, 10000);
            }
        };
    }

    public ObjectViewerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ObjectViewerFragment newInstance(ObjInformation.ObjType type, int id) {
        ObjectViewerFragment fragment = new ObjectViewerFragment();
        Bundle args = new Bundle();
        args.putInt("com.example.museomatematico.ObjType", type.getValue());
        args.putInt("com.example.museomatematico.FragmentId", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alreadyAsked = false;
        callGetOnTouchListener = true;
        if (getArguments() != null) {
            int i = getArguments().getInt("com.example.museomatematico.ObjType");
            mObjInfo = new ObjInformation(ObjInformation.ObjType.from(i));
            SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
            renderer = new ObjRenderer(getActivity(), sensorManager, mObjInfo);

            my_camera = new MyCameraManager();
            my_camera.initCamera(getActivity(),alreadyAsked, renderer);
            alreadyAsked = true;

            handler.post(runnableCode);

            mId = getArguments().getInt("com.example.museomatematico.FragmentId");
            MultiTouchViewPager pager = ((ShowObjActivity) getActivity()).mPager;
            if (pager != null && mId == pager.getCurrentItem()) {
                pager.setOnTouchListener(getOnTouchListener());
                callGetOnTouchListener = false;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_object_viewer, container, false);
        mRajawaliSurface = (SurfaceView) fragmentView.findViewById(R.id.obj_surface);
        mRajawaliSurface.setFrameRate(60);
        mRajawaliSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        mRajawaliSurface.setSurfaceRenderer(renderer);

        return fragmentView;
    }

    @Override
    public void onDestroy(){
        handler.removeCallbacks(runnableCode);
        super.onDestroy();
    }

    public MultiTouchViewPager.OnTouchListener getOnTouchListener() {
        return renderer.getOnTouchListener();
    }
}

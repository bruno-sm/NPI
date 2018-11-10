package com.example.bruno.museomatematico;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.rajawali3d.view.SurfaceView;


public class ObjectViewerFragment extends Fragment {
    private SurfaceView mRajawaliSurface;
    private ObjInformation mObjInfo;
    private ObjRenderer mRenderer;
    public boolean callGetOnTouchListener = false;
    private int mId;


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
        callGetOnTouchListener = true;
        if (getArguments() != null) {
            int i = getArguments().getInt("com.example.museomatematico.ObjType");
            mObjInfo = new ObjInformation(ObjInformation.ObjType.from(i));
            SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
            mRenderer = new ObjRenderer(getActivity(), sensorManager, mObjInfo);

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
        mRajawaliSurface.setSurfaceRenderer(mRenderer);

        return fragmentView;
    }


    public MultiTouchViewPager.OnTouchListener getOnTouchListener() {
        return mRenderer.getOnTouchListener();
    }
}

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

import org.rajawali3d.view.SurfaceView;


public class ObjectViewerFragment extends Fragment {
    private SurfaceView mRajawaliSurface;


    public ObjectViewerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ObjectViewerFragment newInstance(String param1, String param2) {
        ObjectViewerFragment fragment = new ObjectViewerFragment();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
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
        ObjRenderer renderer = new ObjRenderer(getActivity(), sensorManager);
        mRajawaliSurface.setSurfaceRenderer(renderer);

        return fragmentView;
    }
}

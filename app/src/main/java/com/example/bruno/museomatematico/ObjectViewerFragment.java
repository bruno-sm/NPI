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


    public ObjectViewerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ObjectViewerFragment newInstance(ObjInformation.ObjType type) {
        ObjectViewerFragment fragment = new ObjectViewerFragment();
        Bundle args = new Bundle();
        args.putInt("com.example.museomatematico.ObjType", type.getValue());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int i = getArguments().getInt("com.example.museomatematico.ObjType");
            mObjInfo = new ObjInformation(ObjInformation.ObjType.from(i));
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
        ObjRenderer renderer = new ObjRenderer(getActivity(), sensorManager, mObjInfo);
        mRajawaliSurface.setSurfaceRenderer(renderer);

        return fragmentView;
    }
}

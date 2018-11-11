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

/* ObjectViewerFragment es la clase encargada de hacer un fragment por cada objeto a visualizar.
Este objeto se comunica con un renderizador y se llama desde ShowObjActivity, una de sus acciones
es comunicar estos dos objetos, la activity y el renderizador, para que el multitouch vaya
bien. Además, gestiona las múltiples llamadas a la cámara desde un handler y mantiene la información
de los objetos en una variable de tipo ObjInformation. Toda la vista se crea en un objeto de tipo
SurfaceView llamada mRajawaliSurface.
 */


public class ObjectViewerFragment extends Fragment {
    private SurfaceView mRajawaliSurface;
    private ObjInformation mObjInfo;
    public boolean callGetOnTouchListener = false;
    private int mId;

    private boolean alreadyAsked;   // Esta variable se dedica a ver si ya hemos preguntado por
                                        // los permisos de la cámara
    private ObjRenderer renderer;

    /* El objeto my_camera será el gestor de cámara al que llamaremos para que vaya haciendo fotos
     */
    private MyCameraManager my_camera;

    /* A continuación creamos un handler que va a ir llamando a la función de initCamera cada 10
    segundos, para ir haciendo fotos. El código para hacer el handler proviene de
    https://stackoverflow.com/a/40339630
    con unos cambios mínimos para que funcione con nuestra cámara y cambiando el número de segundos
     */
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
                Log.d("Handlers", "Called on main thread");
                // Repeat this the same runnable code block again another 10 seconds
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


    /* Inicializamos el ObjectViewerFragment. Aquí es donde creamos el renderer, iniciamos el
    sensorManager y el mObjInformation para gestionar la información de los objetos, iniciamos la cámara
    y pasamos el TouchListener al ViewPager desde el renderer
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alreadyAsked = false;
        callGetOnTouchListener = true;
        if (getArguments() != null) {
            //Iniciamos los objetos mObjInfo, sensorManager y renderer
            int i = getArguments().getInt("com.example.museomatematico.ObjType");
            mObjInfo = new ObjInformation(ObjInformation.ObjType.from(i));
            SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
            renderer = new ObjRenderer(getActivity(), sensorManager, mObjInfo);

            /* Inicializamos la cámara, le pasamos la actividad de la que depende este fragment,
            el booleano de los permisos y el renderer. Luego iniciamos el handler para que se active
            cada diez segundos
             */
            my_camera = new MyCameraManager();
            my_camera.initCamera(getActivity(),alreadyAsked, renderer);
            alreadyAsked = true;

            handler.post(runnableCode);

            /* Aquí es donde le damos al ViewPager el TouchListener del renderer para que pueda
            hacer las operaciones de rotación, zoom, etc.
             */
            mId = getArguments().getInt("com.example.museomatematico.FragmentId");
            MultiTouchViewPager pager = ((ShowObjActivity) getActivity()).mPager;
            if (pager != null && mId == pager.getCurrentItem()) {
                pager.setOnTouchListener(getOnTouchListener());
                callGetOnTouchListener = false;
            }
        }
    }

    /* onCreateView se llama cuando se crea una View y crea la SurfaceView mRajawaliSurface,
    configurándola de forma correcta. Luego devuelve la View fragmentView que es la vista del fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_object_viewer, container, false);
        mRajawaliSurface = (SurfaceView) fragmentView.findViewById(R.id.obj_surface);
        mRajawaliSurface.setFrameRate(60);
        mRajawaliSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // También iniciamos el sensorManager cogiéndolo de la actividad padre
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        mRajawaliSurface.setSurfaceRenderer(renderer);

        return fragmentView;
    }

    /* Necesitamos destruir el handler de hacer fotos cuando el Fragment se destruye, para que
    pare de intentar comunicarse con el renderer, que va a destruirse también
     */
    @Override
    public void onDestroy(){
        handler.removeCallbacks(runnableCode);
        super.onDestroy();
    }

    public MultiTouchViewPager.OnTouchListener getOnTouchListener() {
        return renderer.getOnTouchListener();
    }
}

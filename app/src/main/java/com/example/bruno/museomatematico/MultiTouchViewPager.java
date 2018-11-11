package com.example.bruno.museomatematico;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

/* MultiTouchViewPager es la clase que se encarga de gestionar los eventos con la pantalla táctil
de los dedos. Es, además, un ViewPager que muestra los objetos de forma que puedes ir haciendo scroll
para poder verlos todos.

La autoría de esta clase no es enteramente nuestra. Parte del código lo hemos cogido de
https://examples.javacodegeeks.com/android/android-multitouch-example/
que contiene un buen ejemplo de Multitouch y es de donde hemos cogido la forma de separar entre
diferentes tipos de movimiento, como zoom o detectar cuándo hay más de un pointer activo.
 */

public class MultiTouchViewPager extends ViewPager {
    // Estas dos variables guardan las últimas posiciones que se han observado en un pointer concreto
    private float mLastXPosition, mLastYPosition;
    // Es una variable que detecta cuando un movimiento puede ser un escalado. En realidad, solo mide
    // si hay más de un pointer en la pantalla
    private boolean scaleMove;
    // Una variable para guardar la última distancia vista entre los pointers, para ver a qué velocidad
    // se mueven
    private double lastdist;
    // Un array con los punteros activos
    private SparseArray mActivePointers = new SparseArray();
    // El listener que detecta cuando los punteros se mueven
    private OnTouchListener onTouchListener;

    // Constructor del MultiTouchViewPager a partir del contexto
    public MultiTouchViewPager(Context context) {
        // Llamamos al constructor del ViewPager
        super(context);
        // Inicializamos las últimas posiciones del puntero a (0,0)
        mLastXPosition = 0f;
        mLastYPosition = 0f;
        // Creamos un nuevo listener que no hace nada, solo Logs
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
            public void onMove(int diffX, int diffY) {
                Log.d("tl", "Move");
            }
        };
    }

    // Segundo constructor del MultiTouchViewPager, que tiene contextos y atributos
    public MultiTouchViewPager(Context context, AttributeSet attrs) {
        // Llamamos al constructor del ViewPager padre
        super(context, attrs);
        // Inicializamos las posiciones últimas del puntero a (0,0)
        mLastXPosition = 0f;
        mLastYPosition = 0f;
        // Inicializamos el listener a uno por defecto que solo hace Log
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
            public void onMove(int diffX, int diffY) {
                Log.d("tl", "Move");
            }
        };
    }


    // Esta función actúa como setter del Touch Listener
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    // onTouchEvent se activa cuando hay un cambio en el sensor de los punteros
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Cogemos el índice y el ID del puntero que se ha activado
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        // Entramos en un switch que distingue cada posible evento que detecte el sensor
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // Se detecta un solo pointer nuevo
            case MotionEvent.ACTION_DOWN:

                mLastYPosition = event.getY();
                mLastXPosition = event.getX();
                onTouchListener.onTouch();
                return super.onTouchEvent(event);
            // Se detecta que todos los pointers han desaparecido
            case MotionEvent.ACTION_UP:
                scaleMove = false;
                lastdist = 0;
                onTouchListener.onRelease();
                return super.onTouchEvent(event);

            // Se detecta un movimiento en un pointer
            case MotionEvent.ACTION_MOVE:
                int diffY = (int) (event.getY() - mLastYPosition);
                int diffX = (int) (event.getX() - mLastXPosition);

                mLastYPosition = event.getY();
                mLastXPosition = event.getX();

                //Check if the action was jitter
                if (Math.abs(diffX) > 4 || Math.abs(diffY) > 4) {

                    // La siguiente función es la que diferencia entre arrastrar dedos o zoom
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
                        } else if (event.getPointerCount() >= 2){
                            res = super.onTouchEvent(event);
                        }

                        lastdist = dist;
                        return res;
                    } else {
                        onTouchListener.onMove(diffX, diffY);
                        return true;
                    }

                }
                break;
            // Se detecta que el pointer ha desaparecido cancelando el evento
            case MotionEvent.ACTION_CANCEL: {
                scaleMove = false;
                mActivePointers.remove(pointerId);
                onTouchListener.onRelease();
                return super.onTouchEvent(event);
            }
            // Se detecta un segundo pointer, así que se activa scaleMove y se añade el pointer
            case MotionEvent.ACTION_POINTER_DOWN: {
                scaleMove = true;
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                mActivePointers.put(pointerId, f);
                return super.onTouchEvent(event);
            }
            default: return super.onTouchEvent(event);
        }

        return false;
    }


    // Creamos una interfaz de OnTouchListener
    public interface OnTouchListener {
        void onTouch();

        void onRelease();

        void onPinchIn();

        void onPinchOut();

        void onMove(int diffX, int diffY);
    }
}

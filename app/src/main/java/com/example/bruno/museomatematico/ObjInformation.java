package com.example.bruno.museomatematico;

import android.util.Log;

import java.util.Dictionary;
import java.util.HashMap;


public class ObjInformation {
    public enum ObjType {
        TORUS(0), KLEIN_BOTTLE(1), MOBIUS_STRIP(2), SPHERE(3), CUBE(4), CYLINDER(5);

        private final int value;

        ObjType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static final HashMap<Integer, ObjType> _map = new HashMap<Integer, ObjType>();
        static
        {
            for (ObjType type : ObjType.values())
                _map.put(type.value, type);
        }

        public static ObjType from(int value)
        {
            return _map.get(value);
        }
    }

    ObjType mType;
    String mName;
    HashMap<String, String> mProperties;
    int mObjFile;


    ObjInformation(ObjType type) {
       setType(type);
    }


    ObjInformation(String n) {
        String name = n;
        if (name.substring(0,1).equals("\"") &&
            name.substring(n.length()-1,n.length()).equals("\""))
            name = n.substring(1, n.length()-1);
        Log.d("ObjInformation", "Crea " + name);

        if(name.equals("Toro"))
            setType(ObjType.TORUS);
        else if(name.equals("Botella de Klein"))
            setType(ObjType.KLEIN_BOTTLE);
        else if(name.equals("Cinta de Moebius"))
            setType(ObjType.MOBIUS_STRIP);
        else if(name.equals("Esfera"))
            setType(ObjInformation.ObjType.SPHERE);
        else if(name.equals("Cubo"))
            setType(ObjInformation.ObjType.CUBE);
        else
            setType(ObjInformation.ObjType.CYLINDER);
    }

    private void setType(ObjType type) {
        mType = type;
        mProperties = new HashMap<String, String>();
        switch (mType) {
            case KLEIN_BOTTLE: mName = "Botella de Klein";
                mObjFile = R.raw.klein;
                mProperties.put("Construcción", "Se parte de un cilindro y luego, en lugar de juntar los extremos del cilindro de forma normal, se insertan en el interior del objeto y se juntan por dentro.");
                mProperties.put("Trivia", "El nombre original en alemán era Kleinsche Fläche (superficie de Klein), pero el traductor original al inglés lo confundió con Kleinsche Flasche (botella de Klein). Como el objeto parece una botella, se quedó al final con ese nombre.");
                mProperties.put("Orientabilidad", "La botella de Klein no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
                break;
            case TORUS: mName = "Toro";
                mObjFile = R.raw.torus;
                mProperties.put("Construcción", "Es una superficie de revolución, es decir, puede formarse partiendo de una circunferencia que rota a lo largo de una recta. También puede constuirse juntando los dos extremos de un cilindro.");
                mProperties.put("Orientabilidad", "El toro es orientable. Si caminas por el borde del toro, nunca llegarás a la otra región del espacio sin hacer un agujero antes en él.");
                mProperties.put("Área", "4 * Pi² * R * r²");
                mProperties.put("Volumen", "2 * Pi² * R * r²");
                mProperties.put("Trivia", "El toto se puede ver como el producto cartesiano de dos circunferencias.");
                break;
            case MOBIUS_STRIP: mName = "Cinta de Möbius";
                mObjFile = R.raw.moebius;
                mProperties.put("Construcción", "Se parte de un rectángulo y se juntan sus extremos opuestos dando media vuelta a uno de ellos.");
                mProperties.put("Trivia", "La cinta de Möbius tiene un solo borde. Si lo sigues recorres todo el borde antes de volver a donde empezaste.");
                mProperties.put("Orientabilidad", "La cinta de Möbius no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
                break;
            case SPHERE: mName = "Esfera";
                mObjFile = R.raw.esfera;
                mProperties.put("Orientabilidad", "La esfera es orientable. Si caminas por el exterior del mundo en el que vivimos nunca puedes llegar al mismo punto pero boca abajo sin hacer un agujero antes.");
                mProperties.put("Ecuación", "x²+y²+z²=1");
                mProperties.put("Forma general", "Los puntos p tal que distancia(p, centro) = radio");
                mProperties.put("Parámetros", "Una esfera viene definida por su centro y radio");
                mProperties.put("Volumen", "4/3 * Pi * r³");
                mProperties.put("Área", "4 Pi * r²");
                break;
            case CUBE: mName = "Cubo";
                mObjFile = R.raw.cubo;
                mProperties.put("Trivia", "El cubo es muy similar a una esfera. Una de sus propiedades comunes es que el cubo se puede ver como una esfera en un espacio tridimensional con una distancia diferente a la usual. En lugar de coger la distancia euclídea cogemos la distancia del máximo de las tres componentes.");
                mProperties.put("Orientabilidad", "El cubo es orientable. Si camináramos a lo largo de un dado gigante, no podríamos entrar en su interior sin hacer primero un agujero en él.");
                mProperties.put("Ecuación", "max{|x|, |y|, |z|} = 1");
                mProperties.put("Forma general", "max{|x-x_0|, |y-y_0|, |z-z_0|} = a");
                mProperties.put("Volumen", "a³");
                break;
            case CYLINDER: mName = "Cilindro";
                mObjFile = R.raw.cilindro;
                mProperties.put("Construcción", "Se puede ver tanto como una superficie de revolución, rotando una recta alrededor de un eje, como el resultado de unir dos extremos opuestos de un rectángulo.");
                mProperties.put("Área", "2 * Pi * r *h");
                mProperties.put("Volumen", "Pi * r² * h");
                mProperties.put("Orientabilidad", "El cilindro es orientable. Si camináramos a lo largo de un cilindro, no podríamos entrar en su interior sin hacer primero un agujero en él.");
                break;
        }
    }

    public ObjType getType(){
        return mType;
    }


    public String getName(){
        return mName;
    }


    public int getObjFile(){
        return mObjFile;
    }


    public HashMap<String, String> getProperties() {
        return mProperties;
    }
}

package com.example.bruno.museomatematico;

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
        mType = type;
        mProperties = new HashMap<String, String>();
        switch (mType) {
            case KLEIN_BOTTLE: mName = "Botella de Klein";
                               mObjFile = R.raw.klein;
                               mProperties.put("construcción", "Se parte de un cilindro y luego, en lugar de juntar los extremos del cilindro de forma normal, se insertan en el interior del objeto y se juntan por dentro.");
                               mProperties.put("trivia", "El nombre original en alemán era Kleinsche Fläche (superficie de Klein), pero el traductor original al inglés lo confundió con Kleinsche Flasche (botella de Klein). Como el objeto parece una botella, se quedó al final con ese nombre.");
                               mProperties.put("orientabilidad", "La botella de Klein no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
                               break;
            case TORUS: mName = "Toro";
                        mObjFile = R.raw.torus;
                        mProperties.put("construcción", "Es una superficie de revolución, es decir, puede formarse partiendo de una circunferencia que rota a lo largo de una recta. También puede constuirse juntando los dos extremos de un cilindro.");
                        mProperties.put("orientabilidad", "El toro es orientable. Si caminas por el borde del toro, nunca llegarás a la otra región del espacio sin hacer un agujero antes en él.");
                        mProperties.put("área", "4 * Pi² * R * r²");
                        mProperties.put("volumen", "2 * Pi² * R * r²");
                        mProperties.put("trivia", "El toto se puede ver como el producto cartesiano de dos circunferencias.");
                        break;
            case MOBIUS_STRIP: mName = "Cinta de Möbius";
                               mObjFile = R.raw.moebius;
                               mProperties.put("construcción", "Se parte de un rectángulo y se juntan sus extremos opuestos dando media vuelta a uno de ellos.");
                               mProperties.put("trivia", "La cinta de Möbius tiene un solo borde. Si lo sigues recorres todo el borde antes de volver a donde empezaste.");
                               mProperties.put("orientabilidad", "La cinta de Möbius no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
                               break;
            case SPHERE: mName = "Esfera";
                mObjFile = R.raw.esfera;
                mProperties.put("orientabilidad", "La esfera es orientable. Si caminas por el exterior del mundo en el que vivimos nunca puedes llegar al mismo punto pero boca abajo sin hacer un agujero antes.");
                mProperties.put("ecuacion", "x²+y²+z²=1");
                mProperties.put("forma general", "Los puntos p tal que distancia(p, centro) = radio");
                mProperties.put("parámetros", "Una esfera viene definida por su centro y radio");
                mProperties.put("volumen", "4/3 * Pi * r³");
                mProperties.put("área", "4 Pi * r²");
                break;
            case CUBE: mName = "Cubo";
                mObjFile = R.raw.cubo;
                mProperties.put("trivia", "El cubo es muy similar a una esfera. Una de sus propiedades comunes es que el cubo se puede ver como una esfera en un espacio tridimensional con una distancia diferente a la usual. En lugar de coger la distancia euclídea cogemos la distancia del máximo de las tres componentes.");
                mProperties.put("orientabilidad", "El cubo es orientable. Si camináramos a lo largo de un dado gigante, no podríamos entrar en su interior sin hacer primero un agujero en él.");
                mProperties.put("ecuación", "max{|x|, |y|, |z|} = 1");
                mProperties.put("ecuación general", "max{|x-x_0|, |y-y_0|, |z-z_0|} = a");
                mProperties.put("volumen", "a³");
                break;
            case CYLINDER: mName = "Cilindro";
                mObjFile = R.raw.cilindro;
                mProperties.put("construcción", "Se puede ver tanto como una superficie de revolución, rotando una recta alrededor de un eje, como el resultado de unir dos extremos opuestos de un rectángulo.");
                mProperties.put("área", "2 * Pi * r *h");
                mProperties.put("volumen", "Pi * r² * h");
                mProperties.put("orientabilidad", "El cilindro es orientable. Si camináramos a lo largo de un cilindro, no podríamos entrar en su interior sin hacer primero un agujero en él.");
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

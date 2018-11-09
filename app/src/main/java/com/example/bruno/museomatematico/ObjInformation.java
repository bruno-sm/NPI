package com.example.bruno.museomatematico;

import java.util.Dictionary;
import java.util.HashMap;

public class ObjInformation {
    public enum ObjType {
        TORUS(0), KLEIN_BOTTLE(2), MOBIUS_STRIP(3);// SPHERE(4), CUBE(5);

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
                        mProperties.put("construcción", "Se parte de un cilindro y luego, en lugar de juntar los extremos del cilindro de forma normal, se insertan en el interior del objeto y se juntan por dentro.");
                        mProperties.put("trivia", "El nombre original en alemán era Kleinsche Fläche (superficie de Klein), pero el traductor original al inglés lo confundió con Kleinsche Flasche (botella de Klein). Como el objeto parece una botella, se quedó al final con ese nombre.");
                        mProperties.put("orientabilidad", "La botella de Klein no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
                        break;
            case MOBIUS_STRIP: mName = "Cinta de Möbius";
                               mObjFile = R.raw.moebius;
                               mProperties.put("construcción", "Se parte de un cilindro y luego, en lugar de juntar los extremos del cilindro de forma normal, se insertan en el interior del objeto y se juntan por dentro.");
                               mProperties.put("trivia", "El nombre original en alemán era Kleinsche Fläche (superficie de Klein), pero el traductor original al inglés lo confundió con Kleinsche Flasche (botella de Klein). Como el objeto parece una botella, se quedó al final con ese nombre.");
                               mProperties.put("orientabilidad", "La botella de Klein no es orientable. Si partes de un punto y empiezas a caminar a lo largo de la superficie, puedes llegar al mismo punto pero boca abajo.");
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

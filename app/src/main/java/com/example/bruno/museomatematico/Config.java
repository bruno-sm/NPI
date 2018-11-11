package com.example.bruno.museomatematico;
/* Toda esta clase proviene del repositorio
https://github.com/dialogflow/dialogflow-android-client/tree/master/apiAISampleApp/src/main/java/ai/api/sample

La autoría del código no es nuestra.

Esta clase se encarga de configurar el bot. La hemos comentado para aclarar para qué sirve cada
parte del código.
 */

/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public abstract class Config {
    /* Incluimos el token de nuestro bot
    */
    // copy this keys from your developer dashboard
    public static final String ACCESS_TOKEN = "101096c066bd400cb4ff31bb3f723b53";

    /* Aquí configuramos el idioma de nuestro bot. Como queremos que hable en español, dejamos
    solo este idioma de todos los que nos aparecían del código original de github
    */
    public static final LanguageConfig[] languages = new LanguageConfig[]{
            new LanguageConfig("es", "49be4c10b6a543dfb41d49d88731bd49"),
    };

    public static final String[] events = new String[]{
            "hello_event",
            "goodbye_event",
            "how_are_you_event"
    };
}

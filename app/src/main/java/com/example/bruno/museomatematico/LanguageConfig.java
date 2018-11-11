package com.example.bruno.museomatematico;
/* Parte de esta clase proviene del repositorio
https://github.com/dialogflow/dialogflow-android-client/tree/master/apiAISampleApp/src/main/java/ai/api/sample

La autoría del código no es nuestra.

Esta clase se encarga de gestionar la configuración del idioma del bot y del token que lo caracteriza.
Solo contiene estos dos objetos con un constructor, un getter de cada uno y un toString. Debido
a que es una clase muy simple y que no es nuestra, hemos querido dejarla comentada con este pequeño
resumen.
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

import java.lang.String;

public class LanguageConfig {
    private final String languageCode;
    private final String accessToken;

    public LanguageConfig(final String languageCode, final String accessToken) {
        this.languageCode = languageCode;
        this.accessToken = accessToken;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return languageCode;
    }
}

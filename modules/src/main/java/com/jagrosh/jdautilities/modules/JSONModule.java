/*
 * Copyright 2016 John Grosh (jagrosh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.modules;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.WillNotClose;
import java.io.InputStream;
import java.net.URLClassLoader;

/**
 * @author Kaidan Gustave
 */
public class JSONModule extends Module<JSONObject>
{
    JSONModule(@WillNotClose URLClassLoader classLoader)
    {
        super(classLoader, cLoader -> {
            final JSONObject json;

            InputStream stream = cLoader.getResourceAsStream("/module.json");
            if(stream == null)
                throw new ModuleException("Could not find module.json!");

            try {
                json = new JSONObject(new JSONTokener(stream));
            } catch(JSONException ex) {
                throw new ModuleException("Encountered an error reading module.json", ex);
            }

            return json;
        });
    }

    @Override
    protected void init(@WillNotClose URLClassLoader classLoader)
    {
        if(moduleConfig.has("name") && !moduleConfig.isNull("name"))
        {
            this.name = moduleConfig.get("name").toString();
        }
    }

    public static class Factory implements ModuleFactory<JSONObject, JSONModule>
    {
        @Override
        public JSONModule create(@WillNotClose URLClassLoader classLoader)
        {
            return new JSONModule(classLoader);
        }

        @Override
        public String getFileExtension()
        {
            return "json";
        }
    }
}

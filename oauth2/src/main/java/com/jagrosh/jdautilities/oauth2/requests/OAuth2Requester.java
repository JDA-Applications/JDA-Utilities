/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
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
package com.jagrosh.jdautilities.oauth2.requests;

import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import net.dv8tion.jda.core.utils.JDALogger;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Kaidan Gustave
 */
public class OAuth2Requester
{
    protected static final Logger LOGGER = JDALogger.getLog(OAuth2Requester.class);
    protected static final String USER_AGENT = "JDA-Utils Oauth2("+JDAUtilitiesInfo.GITHUB+" | "+JDAUtilitiesInfo.VERSION+")";
    protected static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);

    private final OkHttpClient httpClient;

    public OAuth2Requester(OkHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    <T> void submitAsync(OAuth2Action<T> request, Consumer<T> success, Consumer<Throwable> failure)
    {
        httpClient.newCall(request.buildRequest()).enqueue(new Callback()
        {
            @Override
            public void onResponse(Call call, Response response)
            {
                try
                {
                    T value = request.handle(response);
                    if(value != null)
                        success.accept(value);
                }
                catch(Throwable t)
                {
                    failure.accept(t);
                }
                finally
                {
                    response.close();
                }

            }

            @Override
            public void onFailure(Call call, IOException e)
            {
                LOGGER.error("Requester encountered an error when submitting a request!", e);
            }
        });
    }

    <T> T submitSync(OAuth2Action<T> request) throws IOException
    {
        try(Response response = httpClient.newCall(request.buildRequest()).execute())
        {
            return request.handle(response);
        }
    }
}

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
package com.jagrosh.jdautilities.oauth2.state;

import java.util.HashMap;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class DefaultStateController implements StateController
{
    private final static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final HashMap<String,String> states = new HashMap<>();
    
    @Override
    public String generateNewState(String redirectUri)
    {
        String state = randomState();
        states.put(state, redirectUri);
        return state;
    }

    @Override
    public String consumeState(String state)
    {
        return states.remove(state);
    }
    
    private static String randomState()
    {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<10; i++)
            sb.append(CHARACTERS.charAt((int)(Math.random()*CHARACTERS.length())));
        return sb.toString();
    }
}

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

/**
 * Implementable state controller, used for registering states
 * and generating redirect URIs using them.
 *
 * <p>Naturally, states should be unique, and attempting to
 * generate a redirect using a previously used state should
 * return {@code null} instead of a new redirect URI.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public interface StateController
{
    /**
     * Generates a new state string using the provided redirect URI.
     * 
     * @param  redirectUri
     *         The redirect URI that will be used with this state.
     *
     * @return The state string.
     */
    String generateNewState(String redirectUri);
    
    /**
     * Consumes a state to get the corresponding redirect URI.
     *
     * <p>Once this method is called for a specific state, it
     * should return null for all future calls of that same state.
     * 
     * @param  state
     *         The state.
     *
     * @return The redirect URI, or {@code null} if the state does not exist.
     */
    String consumeState(String state);
}

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
package com.jagrosh.jdautilities.oauth2.session;

/**
 * An abstract controller for {@link com.jagrosh.jdautilities.oauth2.session.Session Sessions},
 *
 * Implementations should be able to create their respectively controlled implementations
 * using an instance of {@link com.jagrosh.jdautilities.oauth2.session.SessionData SessionData}
 * and maintain the created instances for the entire lifetime of the session.
 *
 * @param  <S>
 *         The type of the Session for this to handle.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 * @author Kaidan Gustave
 */
public interface SessionController<S extends Session>
{
    /**
     * Gets a {@link com.jagrosh.jdautilities.oauth2.session.Session Session} that
     * was previously created using the provided identifier.
     *
     * <p>It is very important for implementations of SessionController to hold
     * a contract that Sessions created using {@link #createSession(SessionData)}
     * will be maintained and retrievable by external sources at any time.
     *
     * <p>Note that Sessions that have elapsed their effective
     * {@link com.jagrosh.jdautilities.oauth2.session.SessionData#getExpiration() expiration}
     * are not necessary to maintain, unless they have been refreshed in which case they
     * should be updated to reflect this.
     *
     * @param  identifier
     *         The identifier to get a Session by.
     *
     * @return The Session mapped to the identifier provided.
     */
    S getSession(String identifier);

    /**
     * Creates a new {@link com.jagrosh.jdautilities.oauth2.session.Session Session} using
     * the specified {@link com.jagrosh.jdautilities.oauth2.session.SessionData SessionData}.
     *
     * <p>Sessions should be kept mapped outside of just creation so that they can be
     * retrieved using {@link SessionController#getSession(String)} later for further
     * manipulation, as well as to keep updated if they are refreshed.
     *
     * @param  data
     *         The data to create a Session using.
     *
     * @return A new Session.
     */
    S createSession(SessionData data);
}

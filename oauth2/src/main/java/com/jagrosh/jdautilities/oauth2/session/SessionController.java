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

import com.jagrosh.jdautilities.oauth2.Scope;
import java.time.OffsetDateTime;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 * @param <S> the type of the Session object
 */
public interface SessionController<S extends Session>
{
    S getSession(String identifier);
    
    S createSession(String identifier, String accessToken, String refreshToken, String tokenType, OffsetDateTime expiration, Scope[] scopes);
}

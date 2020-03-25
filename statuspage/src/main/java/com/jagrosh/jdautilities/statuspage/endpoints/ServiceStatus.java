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
package com.jagrosh.jdautilities.statuspage.endpoints;

import com.jagrosh.jdautilities.statuspage.data.Page;
import com.jagrosh.jdautilities.statuspage.data.Status;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ServiceStatus
{
    @Nonnull
    protected final Page page;
    @Nonnull
    protected final Status status;

    public ServiceStatus(@Nonnull Page page, @Nonnull Status status)
    {
        this.page = page;
        this.status = status;
    }

    @Nonnull
    public Page getPage()
    {
        return page;
    }

    @Nonnull
    public Status getStatus()
    {
        return status;
    }
}

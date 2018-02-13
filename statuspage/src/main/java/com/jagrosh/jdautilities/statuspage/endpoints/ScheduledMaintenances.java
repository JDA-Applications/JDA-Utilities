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
import com.jagrosh.jdautilities.statuspage.data.ScheduledMaintenance;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Immutable
public class ScheduledMaintenances implements List<ScheduledMaintenance>
{
    @Nonnull
    protected final Page page;
    @Nonnull
    protected final List<ScheduledMaintenance> ScheduledMaintenances;

    public ScheduledMaintenances(@Nonnull Page page, @Nonnull List<ScheduledMaintenance> ScheduledMaintenances)
    {
        this.page = page;
        this.ScheduledMaintenances = ScheduledMaintenances;
    }

    @Nonnull
    public Page getPage()
    {
        return page;
    }

    @Nonnull
    public List<ScheduledMaintenance> getScheduledMaintenances()
    {
        return ScheduledMaintenances;
    }

    @Override
    public int size() {return ScheduledMaintenances.size();}

    @Override
    public boolean isEmpty() {return ScheduledMaintenances.isEmpty();}

    @Override
    public boolean contains(Object o) {return ScheduledMaintenances.contains(o);}

    @Override
    @Nonnull
    public Iterator<ScheduledMaintenance> iterator() {return ScheduledMaintenances.iterator();}

    @Override
    @Nonnull
    public Object[] toArray() {return ScheduledMaintenances.toArray();}

    @Override
    @Nonnull
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@Nonnull T[] a) {return ScheduledMaintenances.toArray(a);}

    @Override
    public boolean add(ScheduledMaintenance ScheduledMaintenance) {return ScheduledMaintenances.add(ScheduledMaintenance);}

    @Override
    public boolean remove(Object o) {return ScheduledMaintenances.remove(o);}

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {return ScheduledMaintenances.containsAll(c);}

    @Override
    public boolean addAll(@Nonnull Collection<? extends ScheduledMaintenance> c) {return ScheduledMaintenances.addAll(c);}

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends ScheduledMaintenance> c) {return ScheduledMaintenances.addAll(index, c);}

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {return ScheduledMaintenances.removeAll(c);}

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {return ScheduledMaintenances.retainAll(c);}

    @Override
    public void clear() {ScheduledMaintenances.clear();}

    @Override
    public int hashCode() {return ScheduledMaintenances.hashCode();}

    @Override
    public ScheduledMaintenance get(int index) {return ScheduledMaintenances.get(index);}

    @Override
    public ScheduledMaintenance set(int index, ScheduledMaintenance element) {return ScheduledMaintenances.set(index, element);}

    @Override
    public void add(int index, ScheduledMaintenance element) {ScheduledMaintenances.add(index, element);}

    @Override
    public ScheduledMaintenance remove(int index) {return ScheduledMaintenances.remove(index);}

    @Override
    public int indexOf(Object o) {return ScheduledMaintenances.indexOf(o);}

    @Override
    public int lastIndexOf(Object o) {return ScheduledMaintenances.lastIndexOf(o);}

    @Override
    @Nonnull
    public ListIterator<ScheduledMaintenance> listIterator() {return ScheduledMaintenances.listIterator();}

    @Override
    @Nonnull
    public ListIterator<ScheduledMaintenance> listIterator(int index) {return ScheduledMaintenances.listIterator(index);}

    @Override
    @Nonnull
    public List<ScheduledMaintenance> subList(int fromIndex, int toIndex) {return ScheduledMaintenances.subList(fromIndex, toIndex);}
}

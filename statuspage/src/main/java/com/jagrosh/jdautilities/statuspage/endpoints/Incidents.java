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

import com.jagrosh.jdautilities.statuspage.data.Incident;
import com.jagrosh.jdautilities.statuspage.data.Page;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Immutable
public class Incidents implements List<Incident>
{
    @Nonnull
    protected final Page page;
    @Nonnull
    protected final List<Incident> incidents;

    public Incidents(@Nonnull Page page, @Nonnull List<Incident> incidents)
    {
        this.page = page;
        this.incidents = incidents;
    }

    @Nonnull
    public Page getPage()
    {
        return page;
    }

    @Nonnull
    public List<Incident> getIncidents()
    {
        return incidents;
    }

    @Override
    public int size() {return incidents.size();}

    @Override
    public boolean isEmpty() {return incidents.isEmpty();}

    @Override
    public boolean contains(Object o) {return incidents.contains(o);}

    @Override
    @Nonnull
    public Iterator<Incident> iterator() {return incidents.iterator();}

    @Override
    @Nonnull
    public Object[] toArray() {return incidents.toArray();}

    @Override
    @Nonnull
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@Nonnull T[] a) {return incidents.toArray(a);}

    @Override
    public boolean add(Incident incident) {return incidents.add(incident);}

    @Override
    public boolean remove(Object o) {return incidents.remove(o);}

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {return incidents.containsAll(c);}

    @Override
    public boolean addAll(@Nonnull Collection<? extends Incident> c) {return incidents.addAll(c);}

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends Incident> c) {return incidents.addAll(index, c);}

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {return incidents.removeAll(c);}

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {return incidents.retainAll(c);}

    @Override
    public void clear() {incidents.clear();}

    @Override
    public int hashCode() {return incidents.hashCode();}

    @Override
    public Incident get(int index) {return incidents.get(index);}

    @Override
    public Incident set(int index, Incident element) {return incidents.set(index, element);}

    @Override
    public void add(int index, Incident element) {incidents.add(index, element);}

    @Override
    public Incident remove(int index) {return incidents.remove(index);}

    @Override
    public int indexOf(Object o) {return incidents.indexOf(o);}

    @Override
    public int lastIndexOf(Object o) {return incidents.lastIndexOf(o);}

    @Override
    @Nonnull
    public ListIterator<Incident> listIterator() {return incidents.listIterator();}

    @Override
    @Nonnull
    public ListIterator<Incident> listIterator(int index) {return incidents.listIterator(index);}

    @Override
    @Nonnull
    public List<Incident> subList(int fromIndex, int toIndex) {return incidents.subList(fromIndex, toIndex);}
}

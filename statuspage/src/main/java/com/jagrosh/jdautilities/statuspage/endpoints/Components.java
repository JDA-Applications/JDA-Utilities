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

import com.jagrosh.jdautilities.statuspage.data.Component;
import com.jagrosh.jdautilities.statuspage.data.Page;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Immutable
public class Components implements List<Component>
{
    @Nonnull
    protected final Page page;
    @Nonnull
    protected final List<Component> components;

    public Components(@Nonnull Page page, @Nonnull List<Component> components)
    {
        this.page = page;
        this.components = components;
    }

    @Nonnull
    public Page getPage()
    {
        return page;
    }

    @Nonnull
    public List<Component> getComponents()
    {
        return components;
    }

    @Override
    public int size() {return components.size();}

    @Override
    public boolean isEmpty() {return components.isEmpty();}

    @Override
    public boolean contains(Object o) {return components.contains(o);}

    @Override
    @Nonnull
    public Iterator<Component> iterator() {return components.iterator();}

    @Override
    @Nonnull
    public Object[] toArray() {return components.toArray();}

    @Override
    @Nonnull
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@Nonnull T[] a) {return components.toArray(a);}

    @Override
    public boolean add(Component component) {return components.add(component);}

    @Override
    public boolean remove(Object o) {return components.remove(o);}

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {return components.containsAll(c);}

    @Override
    public boolean addAll(@Nonnull Collection<? extends Component> c) {return components.addAll(c);}

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends Component> c) {return components.addAll(index, c);}

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {return components.removeAll(c);}

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {return components.retainAll(c);}

    @Override
    public void clear() {components.clear();}

    @Override
    public int hashCode() {return components.hashCode();}

    @Override
    public Component get(int index) {return components.get(index);}

    @Override
    public Component set(int index, Component element) {return components.set(index, element);}

    @Override
    public void add(int index, Component element) {components.add(index, element);}

    @Override
    public Component remove(int index) {return components.remove(index);}

    @Override
    public int indexOf(Object o) {return components.indexOf(o);}

    @Override
    public int lastIndexOf(Object o) {return components.lastIndexOf(o);}

    @Override
    @Nonnull
    public ListIterator<Component> listIterator() {return components.listIterator();}

    @Override
    @Nonnull
    public ListIterator<Component> listIterator(int index) {return components.listIterator(index);}

    @Override
    @Nonnull
    public List<Component> subList(int fromIndex, int toIndex) {return components.subList(fromIndex, toIndex);}
}

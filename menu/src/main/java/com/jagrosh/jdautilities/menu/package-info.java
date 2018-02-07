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

/**
 * Menus package.<br>
 * Contains the {@link com.jagrosh.jdautilities.menu.Menu Menu} class and all
 * standard implementations of it:
 * <ul>
 *     <li>{@link com.jagrosh.jdautilities.menu.ButtonMenu ButtonMenu}
 *     <br>A menu where users select a choice via "reaction-buttons".</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.menu.OrderedMenu OrderedMenu}
 *     <br>A menu with 1 - 10 ordered items, each with their own reaction to choose them with.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.menu.Paginator Paginator}
 *     <br>A menu that paginates a number of items across a number of pages and uses reactions to traverse between them.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog}
 *     <br>A menu that orders choices and uses a indicator and reactions to choose one of the choices.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.menu.Slideshow Slideshow}
 *     <br>A menu similar to the Paginator that displays a picture on each page.</li>
 * </ul>
 *
 * All menus also come with an implementation of a {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder}
 * as a static inner class of the corresponding Menu implementation, which are the main entryway to create said
 * implementations for usage.
 *
 * <p>Please note that this entire package makes <b>HEAVY</b> usage of the
 * {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter}.
 */
package com.jagrosh.jdautilities.menu;

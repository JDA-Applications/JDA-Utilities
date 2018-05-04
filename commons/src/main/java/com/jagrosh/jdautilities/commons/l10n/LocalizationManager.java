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
package com.jagrosh.jdautilities.commons.l10n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @since  2.2.0
 * @author John Grosh
 */
public class LocalizationManager
{
    private static final String DEFAULT_BUNDLE_NAME = "JDAUtilitiesLocalizedText";

    private final String bundleName;
    private final ClassLoader loader;

    public LocalizationManager() { this(DEFAULT_BUNDLE_NAME); }
    
    public LocalizationManager(String bundleName)
    {
        this(bundleName, null);
    }
    
    public LocalizationManager(String bundleName, ClassLoader loader)
    {
        this.bundleName = bundleName;
        this.loader = loader;
    }
    
    public boolean localeExists(Locale locale)
    {
        if (locale == null)
            return false;
        if (locale.equals(Locale.getDefault()))
            return true;
        try
        {
            return locale.equals(load(locale).getLocale());
        }
        catch (MissingResourceException ex)
        {
            return false;
        }
    }
    
    public String format(Localization text, Locale locale, Object... params)
    {
        String str;
        try
        {
            str = load(locale).getString(text.getKey());
        }
        catch (MissingResourceException | ClassCastException | NullPointerException ex)
        {
            str = null;
        }
        if (str == null)
            str = text.getDefaultText();
        return new MessageFormat(str, locale).format(params);
    }
    
    private ResourceBundle load(Locale locale) throws MissingResourceException
    {
        return ResourceBundle.getBundle(bundleName, locale, loader==null ? getClass().getClassLoader() : loader);
    }
}

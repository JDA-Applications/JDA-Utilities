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

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public enum Entities implements Localization
{
    BOT(         "bot",          "Bot"),
    CHANNEL(     "channel",      "Channel"),
    EMBED(       "embed",        "Embed"),
    EMOTE(       "emote",        "Emote"),
    GUILD(       "guild",        "Guild"),
    INVITE(      "invite",       "Invite"),
    MEMBER(      "member",       "Member"),
    MESSAGE(     "message",      "Message"),
    PERMISSION(  "permission",   "Permission"),
    REGION(      "region",       "Region"),
    ROLE(        "role",         "Role"),
    SERVER(      "server",       "Server"),
    TEXTCHANNEL( "textchannel",  "Text Channel"),
    USER(        "user",         "User"),
    VOICECHANNEL("voicechannel", "Voice Channel"),
    WEBHOOK(     "webhook",      "Webhook");
    
    private final String key, defaultText;
    private final Localization plural, number;
    
    private Entities(String key, String defaultText)
    {
        this.key = "entities." + key;
        this.defaultText = defaultText;
        this.plural = new LocalizedText(this.key + ".pl", defaultText + "s");
        this.number = new LocalizedText(this.key + ".num", "{0} " + defaultText + "{0,choice,0#s| 1#| 1<s}");
    }
    
    public Localization plural()
    {
        return plural;
    }
    
    public Localization number()
    {
        return number;
    }
    
    @Override
    public String getDefaultText()
    {
        return defaultText;
    }

    @Override
    public String getKey()
    {
        return key;
    }
}

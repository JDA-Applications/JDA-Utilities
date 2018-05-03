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
package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import com.jagrosh.jdautilities.commons.l10n.Localization;

/**
 *
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
    name = "About",
    description = "Gets information about the bot."
)
@Author("John Grosh (jagrosh)")
public class AboutCommand extends Command 
{
    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;
    
    public AboutCommand(Color color, String description, String[] features, Permission... perms)
    {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }
    
    public void setIsAuthor(boolean value)
    {
        this.IS_AUTHOR = value;
    }
    
    public void setReplacementCharacter(String value)
    {
        this.REPLACEMENT_ICON = value;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if (oauthLink == null) 
        {
            try 
            {
                ApplicationInfo info = event.getJDA().asBot().getApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } 
            catch (Exception e) 
            {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("Could not generate invite link ", e);
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getGuild() == null ? color : event.getGuild().getSelfMember().getColor());
        builder.setAuthor(event.localize(Messages.AUTHOR, event.getSelfUser().getName()), null, event.getSelfUser().getAvatarUrl());
        String author = event.getJDA().getUserById(event.getClient().getOwnerId())==null ? "<@" + event.getClient().getOwnerId()+">" 
                : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        String invline;
        if(event.getClient().getServerInvite() == null || event.getClient().getServerInvite().isEmpty())
        {
            invline = oauthLink.isEmpty() ? "" : "\n" + event.localize(Messages.DESCRIPTION_INVITE, oauthLink);
        }
        else
        {
            invline = "\n" + (oauthLink.isEmpty() 
                    ? event.localize(Messages.DESCRIPTION_JOIN, event.getClient().getServerInvite()) 
                    : event.localize(Messages.DESCRIPTION_JOININVITE, event.getClient().getServerInvite(), oauthLink));
        }
        StringBuilder descr = new StringBuilder().append(event.localize(Messages.DESCRIPTION_HELLO, event.getSelfUser().getName(), description))
                .append("\n").append(event.localize(IS_AUTHOR ? Messages.DESCRIPTION_WRITTEN : Messages.DESCRIPTION_OWNED, author, 
                        JDAUtilitiesInfo.AUTHOR, JDAUtilitiesInfo.GITHUB, JDAUtilitiesInfo.VERSION, JDAInfo.GITHUB, JDAInfo.VERSION))
                .append("\n").append(event.localize(Messages.DESCRIPTION_TYPEHELP, event.getClient().getTextualPrefix(), event.getClient().getHelpWord()))
                .append(invline).append("\n\n").append(event.localize(Messages.DESCRIPTION_FEATURES)).append(" ```css");
        for (String feature : features)
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == null)
        {
            builder.addField(event.localize(Messages.FIELD_STATS), 
                    event.localize(Messages.FIELD_STATS_SERVERTOTAL, event.getJDA().getGuildCache().size()) + "\n" 
                  + event.localize(Messages.FIELD_STATS_1SHARD), true);
            builder.addField(event.localize(Messages.FIELD_USERS), 
                    event.localize(Messages.FIELD_USERS_UNIQUE, event.getJDA().getUserCache().size()) + "\n" 
                  + event.localize(Messages.FIELD_USERS_TOTAL, event.getJDA().getGuildCache().stream().mapToInt(g -> (int)g.getMemberCache().size()).sum()), true);
            builder.addField(event.localize(Messages.FIELD_CHANNELS), 
                    event.localize(Messages.FIELD_CHANNELS_TEXT, event.getJDA().getTextChannelCache().size()) + "\n" 
                  + event.localize(Messages.FIELD_CHANNELS_VOICE, event.getJDA().getVoiceChannelCache().size()), true);
        }
        else
        {
            builder.addField(event.localize(Messages.FIELD_STATS), 
                    event.localize(Messages.FIELD_STATS_SERVERTOTAL, event.getClient().getTotalGuilds()) + "\n"
                  + event.localize(Messages.FIELD_STATS_SHARD, event.getJDA().getShardInfo().getShardId()+1, event.getJDA().getShardInfo().getShardTotal()), true);
            builder.addField(event.localize(Messages.FIELD_THISSHARD), 
                    event.localize(Messages.FIELD_THISSHARD_USERS, event.getJDA().getUserCache().size()) + "\n"
                  + event.localize(Messages.FIELD_THISSHARD_SERVERS, event.getJDA().getGuildCache().size()), true);
            builder.addField("", 
                    event.localize(Messages.FIELD_THISSHARD_TEXT, event.getJDA().getTextChannelCache().size()) + "\n"
                  + event.localize(Messages.FIELD_THISSHARD_VOICE, event.getJDA().getVoiceChannelCache().size()), true);
        }
        builder.setFooter(event.localize(Messages.FOOTER), null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
    
    private enum Messages implements Localization
    {
        DESCRIPTION_HELLO(      "examples.about.description.hello",      "Hello, I am **{0}**, {1}"),
        DESCRIPTION_WRITTEN(    "examples.about.description.written",    "I was written in Java by **{0}** using {1}'s [Commands Extension]({2}) ({3}) and the [JDA library]({4}) ({5})"),
        DESCRIPTION_OWNED(      "examples.about.description.owned",      "I am owned by **{0}** using {1}'s [Commands Extension]({2}) ({3}) and the [JDA library]({4}) ({5})"),
        DESCRIPTION_TYPEHELP(   "examples.about.description.typehelp",   "Type `{0}{1}` to see my commands!"),
        DESCRIPTION_FEATURES(   "examples.about.description.features",   "Some of my features include:"),
        DESCRIPTION_JOIN(       "examples.about.description.join",       "Join my server [`here`]({0})!"),
        DESCRIPTION_INVITE(     "examples.about.description.invite",     "Please [`invite`]({0}) me to your server!"),
        DESCRIPTION_JOININVITE( "examples.about.description.joininvite", "Join my server [`here`]({0}), or [`invite`]({1}) me to your server!"),
        
        AUTHOR(                 "examples.about.author",            "All about {0}!"),
        FOOTER(                 "examples.about.footer",            "Last restart"),
        
        FIELD_STATS(            "examples.about.stats",             "Stats"),
        FIELD_STATS_SERVERTOTAL("examples.about.stats.servertotal", "{0} Servers"),
        FIELD_STATS_SHARD(      "examples.about.stats.shard",       "Shard {0}/{1}"),
        FIELD_STATS_1SHARD(     "examples.about.stats.1shard",      "1 Shard"),
        
        FIELD_USERS(            "examples.about.users",             "Users"),
        FIELD_USERS_UNIQUE(     "examples.about.users.unique",      "{0} unique"),
        FIELD_USERS_TOTAL(      "examples.about.users.total",       "{0} total"),
        
        FIELD_CHANNELS(         "examples.about.channels",          "Channels"),
        FIELD_CHANNELS_TEXT(    "examples.about.channels.text",     "{0} Text"),
        FIELD_CHANNELS_VOICE(   "examples.about.channels.voice",    "{0} Voice"),
        
        FIELD_THISSHARD(        "examples.about.thisshard",         "This shard"),
        FIELD_THISSHARD_USERS(  "examples.about.thisshard.users",   "{0} Users"),
        FIELD_THISSHARD_SERVERS("examples.about.thisshard.servers", "{0} Servers"),
        FIELD_THISSHARD_TEXT(   "examples.about.thisshard.text",    "{0} Text Channels"),
        FIELD_THISSHARD_VOICE(  "examples.about.thisshard.voice",   "{0} Voice Channels");
            
        private final String key, defaultText;
        
        private Messages(String key, String defaultText)
        {
            this.key = key;
            this.defaultText = defaultText;
        }
        
        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public String getDefaultText()
        {
            return defaultText;
        }
    }
}

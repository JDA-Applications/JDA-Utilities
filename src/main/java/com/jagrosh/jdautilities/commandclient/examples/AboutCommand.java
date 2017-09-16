/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.commandclient.examples;

import com.jagrosh.jdautilities.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class AboutCommand extends Command {
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
    protected void execute(CommandEvent event) {
        if (oauthLink == null) {
            try {
                ApplicationInfo info = event.getJDA().asBot().getApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("Could not generate invite link ", e);
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getGuild()==null ? color : event.getGuild().getSelfMember().getColor());
        builder.setAuthor("All about "+event.getSelfUser().getName()+"!", null, event.getSelfUser().getAvatarUrl());
        boolean join = !(event.getClient().getServerInvite()==null || event.getClient().getServerInvite().isEmpty());
        boolean inv =  !oauthLink.isEmpty();
        String invline = "\n"+(join ? "Join my server [`here`]("+event.getClient().getServerInvite()+")" : (inv ? "Please " : ""))+(inv ? (join ? ", or " : "")+"[`invite`]("+oauthLink+") me to your server" : "")+"!";
        String descr = "Hello! I am **"+event.getSelfUser().getName()+"**, "+description
                + "\nI "+(IS_AUTHOR ? "was written in Java" : "am owned")+" by **"+event.getJDA().getUserById(event.getClient().getOwnerId()).getName()
                + "** using "+JDAUtilitiesInfo.AUTHOR+"'s [Commands Extension]("+JDAUtilitiesInfo.GITHUB+") ("+JDAUtilitiesInfo.VERSION+") and the "
                + "[JDA library](https://github.com/DV8FromTheWorld/JDA) ("+JDAInfo.VERSION+")"
                + "\nType `"+event.getClient().getTextualPrefix()+event.getClient().getHelpWord()+"` to see my commands!"
                + (join||inv ? invline : "")
                + "\n\nSome of my features include: ```css";
        for(String feature: features)
            descr+="\n"+(event.getClient().getSuccess().startsWith("<") ? REPLACEMENT_ICON : event.getClient().getSuccess())+" "+feature;
        descr+=" ```";
        builder.setDescription(descr);
        if(event.getJDA().getShardInfo()==null)
        {
            builder.addField("Stats", event.getJDA().getGuilds().size()+" servers\n1 shard", true);
            builder.addField("Users", event.getJDA().getUsers().size()+" unique\n"+event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum()+" total", true);
            builder.addField("Channels", event.getJDA().getTextChannels().size()+" Text\n"+event.getJDA().getVoiceChannels().size()+" Voice", true);
        }
        else
        {
            builder.addField("Stats",(event.getClient()).getTotalGuilds()+" Servers\nShard "
                    +(event.getJDA().getShardInfo().getShardId()+1)+"/"+event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("This shard",event.getJDA().getUsers().size()+" Users\n"+event.getJDA().getGuilds().size()+" Servers", true);
            builder.addField("", event.getJDA().getTextChannels().size()+" Text Channels\n"+event.getJDA().getVoiceChannels().size()+" Voice Channels", true);
        }
        builder.setFooter("Last restart", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
    
}

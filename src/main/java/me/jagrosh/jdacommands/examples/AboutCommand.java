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
package me.jagrosh.jdacommands.examples;

import java.awt.Color;
import me.jagrosh.jdacommands.Command;
import me.jagrosh.jdacommands.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.OnlineStatus;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class AboutCommand extends Command {

    private final Color color;
    private final String description;
    private final String oauthLink;
    private final String[] features;
    
    public AboutCommand(Color color, String description, String oauthLink, String[] features)
    {
        this.color = color;
        this.description = description;
        this.oauthLink = oauthLink;
        this.features = features;
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getGuild()==null ? color : event.getGuild().getSelfMember().getColor());
        builder.setAuthor("All about "+event.getSelfUser().getName()+"!", null, event.getSelfUser().getAvatarUrl());
        String descr = "Hello! I am **"+event.getSelfUser().getName()+"**, "+description
                + "\nI was written in Java by **"+event.getJDA().getUserById(event.getClient().getOwnerId()).getName()
                + "** using jagrosh's [Commands Extension](https://github.com/jagrosh/JDA-Commands) and the "
                + "[JDA library](https://github.com/DV8FromTheWorld/JDA) ("+JDAInfo.VERSION+") <:jda:230988580904763393>"
                + "\nType `"+event.getClient().getTextualPrefix()+"help` to see my commands!"
                + "\nJoin my server [`here`]("+event.getClient().getServerInvite()+"), or [`invite`]("+oauthLink+") me to your server!"
                + "\n\nSome of my features include: ```css";
        for(String feature: features)
            descr+="\n"+event.getClient().getSuccess()+" "+feature;
        descr+=" ```";
        builder.setDescription(descr);
        builder.addField("Stats", event.getJDA().getGuilds().size()+" servers\n"+(event.getJDA().getShardInfo()==null ? "1 shard" : event.getJDA().getShardInfo().getShardTotal()+" shards"), true);
        builder.addField("Users", event.getJDA().getUsers().size()+" unique\n"
                +event.getJDA().getUsers().stream().filter(u -> 
                    {
                        try{
                            OnlineStatus status = event.getJDA().getGuilds().stream().filter(g -> g.isMember(u)).findAny().get().getMember(u).getOnlineStatus();
                            return status == OnlineStatus.ONLINE || status == OnlineStatus.IDLE || status == OnlineStatus.DO_NOT_DISTURB || status == OnlineStatus.INVISIBLE;
                        } catch(Exception e){
                            return false;
                        }
                    }
                ).count()+" online", true);
        builder.addField("Channels", event.getJDA().getTextChannels().size()+" Text\n"+event.getJDA().getVoiceChannels().size()+" Voice", true);
        builder.setFooter("Last restart", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
    
}

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
package me.jagrosh.jdautilities.commandclient.examples;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.awt.Color;
import me.jagrosh.jdautilities.JDAUtilitiesInfo;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class AboutCommand extends Command {
    public static boolean IS_AUTHOR = true;
    private final Color color;
    private final String description;
    private final long perms;
    private String oauthLink;
    private final String[] features;
    
    public AboutCommand(Color color, String description, String[] features, Permission... requestedPerms)
    {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        if(requestedPerms==null)
        {
            this.oauthLink = "";
            this.perms = 0;
        }
        else
        {
            this.oauthLink = null;
            long p = 0;
            for(Permission perm: requestedPerms)
                p += perm.getRawValue();
            perms = p;
        }
    }
    
    @Override
    protected void execute(CommandEvent event) {
        if(oauthLink==null)
        {
            try{
                JSONObject app = Unirest.get("https://discordapp.com/api/oauth2/applications/@me")
                    .header("Authorization", "Bot "+event.getJDA().getToken())
                    .asJson().getBody().getObject();
                oauthLink = "https://discordapp.com/oauth2/authorize?client_id="+app.getString("id")+"&permissions="+perms+"&scope=bot";
            }catch(UnirestException | JSONException e){
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getGuild()==null ? color : event.getGuild().getSelfMember().getColor());
        builder.setAuthor("All about "+event.getSelfUser().getName()+"!", null, event.getSelfUser().getAvatarUrl());
        String descr = "Hello! I am **"+event.getSelfUser().getName()+"**, "+description
                + "\nI "+(IS_AUTHOR ? "was written in Java" : "am owned")+" by **"+event.getJDA().getUserById(event.getClient().getOwnerId()).getName()
                + "** using "+JDAUtilitiesInfo.AUTHOR+"'s [Commands Extension]("+JDAUtilitiesInfo.GITHUB+") ("+JDAUtilitiesInfo.VERSION+") and the "
                + "[JDA library](https://github.com/DV8FromTheWorld/JDA) ("+JDAInfo.VERSION+") <:jda:230988580904763393>"
                + "\nType `"+event.getClient().getTextualPrefix()+"help` to see my commands!"
                + "\nJoin my server [`here`]("+event.getClient().getServerInvite()+")"
                +(oauthLink.isEmpty() ? "" : ", or [`invite`]("+oauthLink+") me to your server")+"!"
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

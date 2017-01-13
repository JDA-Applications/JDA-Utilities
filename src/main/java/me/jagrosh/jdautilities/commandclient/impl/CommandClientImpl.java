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
package me.jagrosh.jdautilities.commandclient.impl;

import com.mashape.unirest.http.Unirest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.Command.Category;
import me.jagrosh.jdautilities.commandclient.CommandClient;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jdautilities.commandclient.CommandListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;

/**
 * This represents a command client, to be used by a bot.
 * 
 * @author John Grosh (jagrosh)
 */
public class CommandClientImpl extends ListenerAdapter implements CommandClient {
    
    private final OffsetDateTime start;
    private final Game game;
    private final String ownerId;
    private final String prefix;
    private final String serverInvite;
    private final ArrayList<Command> commands;
    private final String success;
    private final String warning;
    private final String error;
    private final String carbonKey;
    private final String botsKey;
    
    private String textPrefix;
    private CommandListener listener = null;
    
    public CommandClientImpl(String ownerId, String prefix, Game game, String serverInvite, String success, String warning, String error, String carbonKey, String botsKey, ArrayList<Command> commands)
    {
        Objects.nonNull(ownerId);
        
        this.start = OffsetDateTime.now();
        
        this.ownerId = ownerId;
        this.prefix = prefix;
        this.game = game;
        this.serverInvite = serverInvite;
        this.success = success==null ? "": success;
        this.warning = warning==null ? "": warning;
        this.error = error==null ? "": error;
        this.carbonKey = carbonKey;
        this.botsKey = botsKey;
        this.commands = commands;
    }
    
    @Override
    public void setListener(CommandListener listener)
    {
        this.listener = listener;
    }
    
    @Override
    public CommandListener getListener()
    {
        return listener;
    }

    @Override
    public OffsetDateTime getStartTime()
    {
        return start;
    }
    
    @Override
    public String getOwnerId()
    {
        return ownerId;
    }
    
    @Override
    public String getSuccess()
    {
        return success;
    }
    
    @Override
    public String getWarning()
    {
        return warning;
    }
    
    @Override
    public String getError()
    {
        return error;
    }

    @Override
    public String getServerInvite() 
    {
        return serverInvite;
    }

    @Override
    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public String getTextualPrefix()
    {
        return textPrefix;
    }
    
    @Override
    public void onReady(ReadyEvent event)
    {
        textPrefix = prefix==null ? "@"+event.getJDA().getSelfUser().getName() : prefix;
        event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        if(game!=null)
            event.getJDA().getPresence().setGame("default".equals(game.getName()) ? 
                    Game.of("Type "+textPrefix+"help") : 
                    game);
        sendStats(event.getJDA());
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot())
            return;
        boolean[] isCommand = new boolean[]{false};
        String[] parts = null;
        if(prefix==null)
        {
            if(event.getMessage().getRawContent().startsWith("<@"+event.getJDA().getSelfUser().getId()+">") 
                    || event.getMessage().getRawContent().startsWith("<@!"+event.getJDA().getSelfUser().getId()+">"))
                parts = Arrays.copyOf(event.getMessage().getRawContent().substring(event.getMessage().getRawContent().indexOf(">")+1).trim().split("\\s+",2), 2);
        }
        else
        {
            if(event.getMessage().getRawContent().toLowerCase().startsWith(prefix.toLowerCase()))
                parts = Arrays.copyOf(event.getMessage().getRawContent().substring(prefix.length()).trim().split("\\s+",2), 2);
        }
        if(parts!=null) //starts with valid prefix
        {
            if(parts[0].equalsIgnoreCase("help"))
            {
                isCommand[0] = true;
                CommandEvent cevent = new CommandEvent(event, parts[1]==null ? "" : parts[1], this);
                if(listener!=null)
                    listener.onCommand(cevent, null);
                StringBuilder builder = new StringBuilder("**"+event.getJDA().getSelfUser().getName()+"** commands:\n");
                Category category = null;
                for(Command command : commands)
                    if(!command.isOwnerCommand() || event.getAuthor().getId().equals(ownerId))
                    {
                        if(!Objects.equals(category, command.getCategory()))
                        {
                            category = command.getCategory();
                            builder.append("\n\n  __").append(category==null ? "No Category" : category.getName()).append("__:\n");
                        }
                        builder.append("\n`").append(textPrefix).append(prefix==null?" ":"").append(command.getName())
                                .append(command.getArguments()==null ? "`" : " "+command.getArguments()+"`")
                                .append(" - ").append(command.getHelp());
                    }
                User owner = event.getJDA().getUserById(ownerId);
                if(owner!=null)
                {
                    builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
                    if(serverInvite!=null)
                        builder.append(" or join ").append(serverInvite);
                }
                if(!event.getAuthor().hasPrivateChannel())
                {
                    event.getAuthor().openPrivateChannel().queue(
                        pc -> pc.sendMessage(builder.toString()).queue( 
                            m-> {if(event.getGuild()!=null) cevent.reactSuccess();},
                            t-> event.getChannel().sendMessage(warning+" Help cannot be sent because you are blocking Direct Messages.").queue()), 
                        t-> event.getChannel().sendMessage(warning+" Help cannot be sent because I could not open a Direct Message with you.").queue());
                }
                else
                {
                    event.getAuthor().getPrivateChannel().sendMessage(builder.toString()).queue(
                        m-> {if(event.getGuild()!=null) cevent.reactSuccess();}, 
                        t-> event.getChannel().sendMessage(warning+" Help cannot be sent because you are blocking Direct Messages.").queue());
                }
                if(listener!=null)
                    listener.onCompletedCommand(cevent, null);
            }
            else
            {
                String name = parts[0];
                String args = parts[1]==null ? "" : parts[1];
                commands.stream().filter(cmd -> cmd.isCommandFor(name)).findAny().ifPresent(command -> {
                    isCommand[0] = true;
                    CommandEvent cevent = new CommandEvent(event, args, this);
                    if(listener!=null)
                        listener.onCommand(cevent, command);
                    if(isAllowed(command, event.getTextChannel()))
                        command.run(cevent);
                    else
                        cevent.reply(error+" That command cannot be used in this channel!");
                });
            }
        }
        if(!isCommand[0] && listener!=null)
            listener.onNonCommandMessage(event);
    }

    private boolean isAllowed(Command command, TextChannel channel)
    {
        if(channel==null)
            return true;
        String topic = channel.getTopic();
        if(topic==null || topic.isEmpty())
            return true;
        topic = topic.toLowerCase();
        String lowerName = command.getName().toLowerCase();
        if(topic.contains("{"+lowerName+"}"))
            return true;
        if(topic.contains("{-"+lowerName+"}"))
            return false;
        String lowerCat = command.getCategory()==null ? null : command.getCategory().getName().toLowerCase();
        if(lowerCat!=null)
        {
            if(topic.contains("{"+lowerCat+"}"))
                return true;
            if(topic.contains("{-"+lowerCat+"}"))
                return false;
        }
        return !topic.contains("{-all}");
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        sendStats(event.getJDA());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        sendStats(event.getJDA());
    }
    
    private void sendStats(JDA jda)
    {
        if(carbonKey!=null)
            Unirest.post("https://www.carbonitex.net/discord/data/botdata.php")
                    .field("key", carbonKey)
                    .field("servercount", jda.getGuilds().size())
                    .asJsonAsync();
        if(botsKey!=null)
            Unirest.post("https://bots.discord.pw/api/bots/"+jda.getSelfUser().getId()+"/stats")
                    .header("Authorization", botsKey)
                    .header("Content-Type","application/json")
                    .body(new JSONObject().put("server_count",jda.getGuilds().size()).toString())
                    .asJsonAsync();
    }
}

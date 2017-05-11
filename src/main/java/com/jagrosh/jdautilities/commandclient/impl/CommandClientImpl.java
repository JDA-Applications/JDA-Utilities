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
package com.jagrosh.jdautilities.commandclient.impl;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.Command.Category;
import com.jagrosh.jdautilities.commandclient.CommandClient;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.commandclient.CommandListener;
import com.jagrosh.jdautilities.utils.SafeIdUtil;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An implementation of {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}, 
 * to be used by a bot.
 * 
 * <p>This is a listener usable with {@link net.dv8tion.jda.core.JDA}, as it extends 
 * {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter} in order to 
 * catch and wrap {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}s, 
 * this CommandClient, and automatically trimmed arguments, then provide them to a command for running
 * and execution.
 * 
 * @author John Grosh (jagrosh)
 */
public class CommandClientImpl extends ListenerAdapter implements CommandClient {
    
    private static final SimpleLog LOG = SimpleLog.getLog("CommandClient");
    
    private final OffsetDateTime start;
    private final Game game;
    private final String ownerId;
    private final String[] coOwnerIds;
    private final String prefix;
    private final String serverInvite;
    private final ArrayList<Command> commands;
    private final String success;
    private final String warning;
    private final String error;
    private final String carbonKey;
    private final String botsKey;
    private final HashMap<String,OffsetDateTime> cooldowns;
    private final HashMap<String,Integer> uses;
    private final HashMap<String,ScheduledFuture<?>> schedulepool;
    private final boolean useHelp;
    private final Function<CommandEvent,String> helpFunction;
    private final String helpWord;
    private final ScheduledExecutorService executor;
    
    private String textPrefix;
    private CommandListener listener = null;
    private int totalGuilds;
    
    public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, Game game, String serverInvite, String success, 
            String warning, String error, String carbonKey, String botsKey, ArrayList<Command> commands, 
            boolean useHelp, Function<CommandEvent,String> helpFunction, String helpWord, ScheduledExecutorService executor)
    {
        Objects.nonNull(ownerId);
        
        if(!SafeIdUtil.checkId(ownerId))
            LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!", ownerId));
        
        for(String coOwnerId : coOwnerIds)
        {
            if(SafeIdUtil.checkId(coOwnerId))
                LOG.warn(String.format("The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!", coOwnerId));
        }
        
        this.start = OffsetDateTime.now();
        
        this.ownerId = ownerId;
        this.coOwnerIds = coOwnerIds;
        this.prefix = prefix;
        this.game = game;
        this.serverInvite = serverInvite;
        this.success = success==null ? "": success;
        this.warning = warning==null ? "": warning;
        this.error = error==null ? "": error;
        this.carbonKey = carbonKey;
        this.botsKey = botsKey;
        this.commands = commands;
        this.cooldowns = new HashMap<>();
        this.uses = new HashMap<>();
        this.schedulepool = new HashMap<>();
        this.useHelp = useHelp;
        this.helpWord = helpWord==null ? "help" : helpWord;
        this.executor = executor==null ? Executors.newSingleThreadScheduledExecutor() : executor;
        this.helpFunction = helpFunction==null ? (event) -> {
                StringBuilder builder = new StringBuilder("**"+event.getSelfUser().getName()+"** commands:\n");
                Category category = null;
                for(Command command : commands)
                    if(!command.isOwnerCommand() || event.isOwner() || event.isCoOwner())
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
                return builder.toString();} : helpFunction;
        if(carbonKey!=null || botsKey!=null)
        {
            Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        }
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
    public List<Command> getCommands()
    {
        return commands;
    }
    
    @Override
    public OffsetDateTime getStartTime()
    {
        return start;
    }

    @Override
    public OffsetDateTime getCooldown(String name)
    {
        return cooldowns.get(name);
    }
    
    @Override
    public int getRemainingCooldown(String name)
    {
        if(cooldowns.containsKey(name))
        {
            int time = (int)OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.SECONDS);
            if(time<=0)
            {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }
    
    @Override
    public void applyCooldown(String name, int seconds)
    {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    @Override
    public void cleanCooldowns()
    {
        OffsetDateTime now = OffsetDateTime.now();
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now))).collect(Collectors.toList()).stream().forEach(str -> cooldowns.remove(str));
    }
    
    @Override
    public int getCommandUses(Command command)
    {
    	return getCommandUses(command.getName());
    }
    
    @Override
    public int getCommandUses(String name)
    {
    	return uses.getOrDefault(name, 0);
    }
    
    @Override
    public String getOwnerId()
    {
        return ownerId;
    }
    
    @Override
    public long getOwnerIdLong()
    {
        return Long.parseLong(ownerId);
    }
    
    @Override
    public String[] getCoOwnerIds()
    {
    	return coOwnerIds;
    }
    
    @Override
    public long[] getCoOwnerIdsLong()
    {
        long[] ids = new long[coOwnerIds.length-1];
        for(int i = 0; i<coOwnerIds.length; i++)
        {
            ids[i] = Long.parseLong(coOwnerIds[i]);
        }
        return ids;
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
    public int getTotalGuilds()
    {
        return totalGuilds;
    }
    
    @Override
    public String getHelpWord()
    {
        return helpWord;
    }
    
    @Override
    public void schedule(String name, int delay, RestAction<?> toQueue)
    {
        saveFuture(name, toQueue.queueAfter(delay, TimeUnit.SECONDS, executor));
    }
    
    @Override
    public void schedule(String name, int delay, Runnable runnable)
    {
        saveFuture(name, executor.schedule(runnable, delay, TimeUnit.SECONDS));
    }
    
    @Override
    public void schedule(String name, int delay, TimeUnit unit, RestAction<?> toQueue)
    {
        saveFuture(name, toQueue.queueAfter(delay, unit, executor));
    }
    
    @Override
    public void schedule(String name, int delay, TimeUnit unit, Runnable runnable)
    {
        saveFuture(name, executor.schedule(runnable, delay, unit));
    }
    
    @Override
    public void saveFuture(String name, ScheduledFuture<?> future)
    {
        synchronized(schedulepool) {
            schedulepool.put(name, future);
        }
    }
    
    @Override
    public boolean scheduleContains(String name)
    {
        synchronized(schedulepool) {
            return schedulepool.containsKey(name);
        }
    }
    
    @Override
    public void cancel(String name)
    {
        synchronized(schedulepool) {
            schedulepool.get(name).cancel(false);
        }
    }
    
    @Override
    public void cancelImmediately(String name)
    {
        synchronized(schedulepool) {
            schedulepool.get(name).cancel(true);
        }
    }
    
    @Override
    public ScheduledFuture<?> getScheduledFuture(String name)
    {
        synchronized(schedulepool) {
            return schedulepool.get(name);
        }
    }
    
    @Override
    public void cleanSchedule()
    {
        synchronized(schedulepool) {
            schedulepool.keySet().stream().filter((str) -> (schedulepool.get(str)).isCancelled() || (schedulepool.get(str)).isDone());
        }
    }
    
    @Override
    public void onReady(ReadyEvent event)
    {
        textPrefix = prefix==null ? "@"+event.getJDA().getSelfUser().getName()+" " : prefix;
        event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        if(game!=null)
            event.getJDA().getPresence().setGame("default".equals(game.getName()) ? 
                    Game.of("Type "+textPrefix+"help") : 
                    game);
        sendStats(event.getJDA());
    }
    
    @Override
    public void onShutdown(ShutdownEvent event)
    {
        executor.shutdown();
        System.exit(0);
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
            if(useHelp && parts[0].equalsIgnoreCase(helpWord))
            {
                isCommand[0] = true;
                CommandEvent cevent = new CommandEvent(event, parts[1]==null ? "" : parts[1], this);
                if(listener!=null)
                    listener.onCommand(cevent, null);
                List<String> messages = CommandEvent.splitMessage(helpFunction.apply(cevent));
                event.getAuthor().openPrivateChannel().queue(
                    pc -> {
                        pc.sendMessage(messages.get(0)).queue( 
                            m-> {
                                if(event.getGuild()!=null)
                                    cevent.reactSuccess();
                                for(int i=1; i<messages.size(); i++)
                                    pc.sendMessage(messages.get(i)).queue();
                            },t-> event.getChannel().sendMessage(warning+" Help cannot be sent because you are blocking Direct Messages.").queue());}, 
                    t-> event.getChannel().sendMessage(warning+" Help cannot be sent because I could not open a Direct Message with you.").queue());
                if(listener!=null)
                    listener.onCompletedCommand(cevent, null);
            }
            else if(event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk())
            {
                String name = parts[0];
                String args = parts[1]==null ? "" : parts[1];
                commands.stream().filter(cmd -> cmd.isCommandFor(name)).findAny().ifPresent(command -> {
                    isCommand[0] = true;
                    CommandEvent cevent = new CommandEvent(event, args, this);
                    
                    if(isAllowed(command, event.getTextChannel()))
                    {
                        if(listener!=null)
                            listener.onCommand(cevent, command);
                        uses.put(command.getName(), uses.getOrDefault(command.getName(),0)+1);
                        command.run(cevent);
                    }
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
    public void onGuildJoin(GuildJoinEvent event)
    {
        if(event.getGuild().getSelfMember().getJoinDate().plusMinutes(10).isAfter(OffsetDateTime.now()));
            sendStats(event.getJDA());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event)
    {
        sendStats(event.getJDA());
    }
    
    private void sendStats(JDA jda)
    {
        if(jda.getShardInfo()==null)
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
        else
        {
            if(carbonKey!=null)
            {
                Unirest.post("https://www.carbonitex.net/discord/data/botdata.php")
                        .field("key", carbonKey)
                        .field("shard_id", jda.getShardInfo().getShardId())
                        .field("shard_count", jda.getShardInfo().getShardTotal())
                        .field("servercount", jda.getGuilds().size())
                        .asJsonAsync();
            }
            if(botsKey!=null)
            {
                Unirest.post("https://bots.discord.pw/api/bots/"+jda.getSelfUser().getId()+"/stats")
                        .header("Authorization", botsKey)
                        .header("Content-Type","application/json")
                        .body(new JSONObject()
                                .put("shard_id", jda.getShardInfo().getShardId())
                                .put("shard_count", jda.getShardInfo().getShardTotal())
                                .put("server_count",jda.getGuilds().size())
                                .toString())
                        .asJsonAsync();
                try {
                    JSONArray array = Unirest.get("https://bots.discord.pw/api/bots/"+jda.getSelfUser().getId()+"/stats")
                            .header("Authorization", botsKey)
                            .header("Content-Type","application/json")
                            .asJson().getBody().getObject().getJSONArray("stats");
                    int total = 0;
                    for(int i=0; i<array.length(); i++)
                        total += array.getJSONObject(i).getInt("server_count");
                    this.totalGuilds = total;
                } catch (UnirestException | JSONException ex) {
                    SimpleLog.getLog("BotList").warn("Failed to retrieve bot shard information from bots.discord.pw");
                }
            }
        }
    }
}

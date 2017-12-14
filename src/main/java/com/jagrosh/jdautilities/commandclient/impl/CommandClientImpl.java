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

import com.jagrosh.jdautilities.commandclient.*;
import com.jagrosh.jdautilities.commandclient.Command.Category;
import com.jagrosh.jdautilities.entities.FixedSizeCache;
import com.jagrosh.jdautilities.utils.SafeIdUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.RestAction;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * An implementation of {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} to be used by a bot.
 * 
 * <p>This is a listener usable with {@link net.dv8tion.jda.core.JDA JDA}, as it extends {@link net.dv8tion.jda.core.hooks.ListenerAdapter
 * ListenerAdapter} in order to catch and wrap {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}s,
 * this CommandClient, and automatically processed arguments, then provide them to a {@link com.jagrosh.jdautilities.commandclient.Command
 * Command} for running and execution.
 * 
 * @author John Grosh (jagrosh)
 */
public class CommandClientImpl extends ListenerAdapter implements CommandClient
{
    private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
    private static final int INDEX_LIMIT = 20;
    private static final String DEFAULT_PREFIX = "@mention";

    private final OffsetDateTime start;
    private final Game game;
    private final OnlineStatus status;
    private final String ownerId;
    private final String[] coOwnerIds;
    private final String prefix;
    private final String altprefix;
    private final String serverInvite;
    private final HashMap<String, Integer> commandIndex;
    private final ArrayList<Command> commands;
    private final String success;
    private final String warning;
    private final String error;
    private final String carbonKey;
    private final String botsKey;
    private final String botsOrgKey;
    private final HashMap<String,OffsetDateTime> cooldowns;
    private final HashMap<String,Integer> uses;
    private final HashMap<String,ScheduledFuture<?>> schedulepool;
    private final FixedSizeCache<Long, Set<Message>> linkMap;
    private final boolean useHelp;
    private final Function<CommandEvent,String> helpFunction;
    private final String helpWord;
    private final ScheduledExecutorService executor;
    private final int linkedCacheSize;
    private final AnnotatedModuleCompiler compiler;

    private String textPrefix;
    private CommandListener listener = null;
    private int totalGuilds;

    public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, String altprefix, Game game, OnlineStatus status, String serverInvite,
            String success, String warning, String error, String carbonKey, String botsKey, String botsOrgKey, ArrayList<Command> commands,
            boolean useHelp, Function<CommandEvent,String> helpFunction, String helpWord, ScheduledExecutorService executor,
            int linkedCacheSize, AnnotatedModuleCompiler compiler)
    {
        if(ownerId == null)
            throw new IllegalArgumentException("Owner ID was set null or not set! Please provide an User ID to register as the owner!");

        if(!SafeIdUtil.checkId(ownerId))
            LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!", ownerId));

        if(coOwnerIds!=null) {
            for(String coOwnerId : coOwnerIds) {
                if(!SafeIdUtil.checkId(coOwnerId))
                    LOG.warn(String.format("The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!", coOwnerId));
            }
        }

        this.start = OffsetDateTime.now();

        this.ownerId = ownerId;
        this.coOwnerIds = coOwnerIds;
        this.prefix = prefix==null || prefix.isEmpty() ? DEFAULT_PREFIX : prefix;
        this.altprefix = altprefix==null || altprefix.isEmpty() ? null : altprefix;
        this.textPrefix = prefix;
        this.game = game;
        this.status = status;
        this.serverInvite = serverInvite;
        this.success = success==null ? "": success;
        this.warning = warning==null ? "": warning;
        this.error = error==null ? "": error;
        this.carbonKey = carbonKey;
        this.botsKey = botsKey;
        this.botsOrgKey = botsOrgKey;
        this.commandIndex = new HashMap<>();
        this.commands = new ArrayList<>();
        this.cooldowns = new HashMap<>();
        this.uses = new HashMap<>();
        this.schedulepool = new HashMap<>();
        this.linkMap = linkedCacheSize>0 ? new FixedSizeCache<>(linkedCacheSize) : null;
        this.useHelp = useHelp;
        this.helpWord = helpWord==null ? "help" : helpWord;
        this.executor = executor==null ? Executors.newSingleThreadScheduledExecutor() : executor;
        this.linkedCacheSize = linkedCacheSize;
        this.compiler = compiler;
        this.helpFunction = helpFunction==null ? (event) -> {
                StringBuilder builder = new StringBuilder("**"+event.getSelfUser().getName()+"** commands:\n");
                Category category = null;
                for(Command command : commands)
                    if(!command.isHidden() && (!command.isOwnerCommand() || event.isOwner()))
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
            java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        }
        for(Command command : commands) {
            addCommand(command);
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
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now)))
                .collect(Collectors.toList()).stream().forEach(str -> cooldowns.remove(str));
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
    public void addCommand(Command command)
    {
        addCommand(command, commands.size());
    }

    @Override
    public void addCommand(Command command, int index)
    {
        if(index>commands.size() || index<0)
            throw new ArrayIndexOutOfBoundsException("Index specified is invalid: ["+index+"/"+commands.size()+"]");
        String name = command.getName();
        synchronized(commandIndex)
        {
            if(commandIndex.containsKey(name))
                throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \""+name+"\"!");
            for(String alias : command.getAliases())
            {
                if(commandIndex.containsKey(alias))
                    throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \""+alias+"\"!");
                commandIndex.put(alias, index);
            }
            commandIndex.put(name, index);
            if(index<commands.size())
                commandIndex.keySet().stream().filter(key -> commandIndex.get(key)>index).collect(Collectors.toList())
                        .forEach(key -> commandIndex.put(key, commandIndex.get(key)+1));
        }
        commands.add(index,command);
    }

    @Override
    public void removeCommand(String name)
    {
        if(!commandIndex.containsKey(name))
            throw new IllegalArgumentException("Name provided is not indexed: \"" + name + "\"!");
        int targetIndex = commandIndex.remove(name);
        if(commandIndex.containsValue(targetIndex))
            commandIndex.keySet().stream().filter(key -> commandIndex.get(key) == targetIndex)
                    .collect(Collectors.toList()).forEach(key -> commandIndex.remove(key));
        commandIndex.keySet().stream().filter(key -> commandIndex.get(key)>targetIndex).collect(Collectors.toList())
                .forEach(key -> commandIndex.put(key, commandIndex.get(key)-1));
        commands.remove(targetIndex);
    }

    @Override
    public void addAnnotatedModule(Object module)
    {
        compiler.compile(module).forEach(command -> addCommand(command));
    }

    @Override
    public void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction)
    {
        compiler.compile(module).forEach(command -> addCommand(command, mapFunction.apply(command)));
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
        if(coOwnerIds==null)
            return null;
        long[] ids = new long[coOwnerIds.length];
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
    public <T> void schedule(String name, int delay, RestAction<T> toQueue)
    {
        saveFuture(name, toQueue.queueAfter(delay, TimeUnit.SECONDS, executor));
    }

    @Override
    public void schedule(String name, int delay, Runnable runnable)
    {
        saveFuture(name, executor.schedule(runnable, delay, TimeUnit.SECONDS));
    }

    @Override
    public <T> void schedule(String name, int delay, TimeUnit unit, RestAction<T> toQueue)
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
        schedulepool.put(name, future);
    }

    @Override
    public boolean scheduleContains(String name)
    {
        return schedulepool.containsKey(name);
    }

    @Override
    public void cancel(String name)
    {
        schedulepool.get(name).cancel(false);
    }

    @Override
    public void cancelImmediately(String name)
    {
        schedulepool.get(name).cancel(true);
    }

    @Override
    public ScheduledFuture<?> getScheduledFuture(String name)
    {
        return schedulepool.get(name);
    }

    @Override
    public void cleanSchedule()
    {
        schedulepool.keySet().stream()
                .filter((str) -> schedulepool.get(str).isCancelled() || schedulepool.get(str).isDone())
                .collect(Collectors.toList()).stream().forEach((str) -> schedulepool.remove(str));
    }

    @Override
    public boolean usesLinkedDeletion() {
        return linkedCacheSize>0;
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        if(!event.getJDA().getSelfUser().isBot())
        {
            LOG.error("JDA-Utilities does not support CLIENT accounts.");
            event.getJDA().shutdown();
            return;
        }
        textPrefix = prefix.equals(DEFAULT_PREFIX) ? "@"+event.getJDA().getSelfUser().getName()+" " : prefix;
        event.getJDA().getPresence().setStatus(status==null ? OnlineStatus.ONLINE : status);
        if(game!=null)
            event.getJDA().getPresence().setGame("default".equals(game.getName()) ?
                    Game.playing("Type "+textPrefix+helpWord) :
                    game);
        sendStats(event.getJDA());
    }

    @Override
    public void onShutdown(ShutdownEvent event)
    {
        executor.shutdown();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot())
            return;
        boolean[] isCommand = new boolean[]{false};
        String[] parts = null;
        String rawContent = event.getMessage().getContentRaw();
        if(prefix.equals(DEFAULT_PREFIX) || (altprefix!=null && altprefix.equals(DEFAULT_PREFIX)))
        {
            if(rawContent.startsWith("<@"+event.getJDA().getSelfUser().getId()+">")
                    || rawContent.startsWith("<@!"+event.getJDA().getSelfUser().getId()+">"))
                parts = Arrays.copyOf(rawContent.substring(rawContent.indexOf(">")+1).trim().split("\\s+",2), 2);
        }
        if(parts==null && rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
            parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+",2), 2);
        if(parts==null && altprefix!=null && rawContent.toLowerCase().startsWith(altprefix.toLowerCase()))
            parts = Arrays.copyOf(rawContent.substring(altprefix.length()).trim().split("\\s+",2), 2);
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
                if(commands.size()<INDEX_LIMIT+1) {
                    commands.stream().filter(cmd -> cmd.isCommandFor(name)).findAny().ifPresent(command -> {
                        isCommand[0] = true;
                        CommandEvent cevent = new CommandEvent(event, args, this);

                        if(listener != null)
                            listener.onCommand(cevent, command);
                        uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
                        command.run(cevent);
                    });
                } else {
                    int i = commandIndex.getOrDefault(name.toLowerCase(), -1);
                    if(i!=-1)
                    {
                        isCommand[0] = true;
                        Command command = commands.get(i);
                        CommandEvent cevent = new CommandEvent(event,args,this);
                        if(listener != null)
                            listener.onCommand(cevent,command);
                        uses.put(command.getName(), uses.getOrDefault(command.getName(), 0)+1);
                        command.run(cevent);
                    }
                }
            }
        }
        if(!isCommand[0] && listener!=null)
            listener.onNonCommandMessage(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event)
    {
        if(event.getGuild().getSelfMember().getJoinDate().plusMinutes(10).isAfter(OffsetDateTime.now()))
            sendStats(event.getJDA());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event)
    {
        sendStats(event.getJDA());
    }

    private void sendStats(JDA jda)
    {
        Logger log = LoggerFactory.getLogger("BotList");
        OkHttpClient client = ((JDAImpl) jda).getHttpClientBuilder().build();

        if (carbonKey != null) {
            FormBody.Builder bodyBuilder = new FormBody.Builder()
                    .add("key", carbonKey)
                    .add("servercount", Integer.toString(jda.getGuilds().size()));
            
            if (jda.getShardInfo() != null)
                bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
                           .add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));
                
            Request.Builder builder = new Request.Builder()
                    .post(bodyBuilder.build())
                    .url("https://www.carbonitex.net/discord/data/botdata.php");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    log.info("Successfully send information to carbonitex.net");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send information to carbonitex.net ", e);
                }
            });
        }
        
        if (botsOrgKey != null) {
            JSONObject body = new JSONObject().put("server_count", jda.getGuilds().size());
            if (jda.getShardInfo() != null)
                body.put("shard_id", jda.getShardInfo().getShardId()).put("shard_count", jda.getShardInfo().getShardTotal());
            
            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsOrgKey)
                    .header("Content-Type", "application/json");
            
            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    log.info("Successfully send information to discordbots.org");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send information to discordbots.org ", e);
                }
            });
        }
        
        if (botsKey != null) {
            JSONObject body = new JSONObject().put("server_count", jda.getGuilds().size());

            if (jda.getShardInfo() != null)
                body.put("shard_id", jda.getShardInfo().getShardId()).put("shard_count", jda.getShardInfo().getShardTotal());

            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsKey)
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    log.info("Successfully send information to bots.discord.pw");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Failed to send information to bots.discord.pw ", e);
                }
            });

            if(jda.getShardInfo()==null)
                this.totalGuilds = jda.getGuilds().size();
            else
            {
                try (Reader reader = client.newCall(new Request.Builder()
                        .get()
                        .url("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
                        .header("Authorization", botsKey)
                        .header("Content-Type", "application/json")
                        .build()).execute().body().charStream()) {
                    JSONArray array = new JSONObject(new JSONTokener(reader)).getJSONArray("stats");
                    int total = 0;
                    for (int i = 0; i < array.length(); i++)
                        total += array.getJSONObject(i).getInt("server_count");
                    this.totalGuilds = total;
                } catch (Exception e) {
                    log.error("Failed to retrieve bot shard information from bots.discord.pw ", e);
                }
            }
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event)
    {
        if(!event.isFromType(ChannelType.TEXT) || !usesLinkedDeletion()) // If it's not from a textchannel
            return;
        synchronized (linkMap)
        {
            if(linkMap.contains(event.getMessageIdLong()))
            {
                Set<Message> messages = linkMap.get(event.getMessageIdLong());
                if(messages.size()>1 && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
                    event.getTextChannel().deleteMessages(messages).queue(unused -> {}, ignored -> {});
                else if(messages.size()>0)
                    messages.forEach(m -> m.delete().queue(unused -> {}, ignored -> {}));
            }
        }
    }

    /**
     * <b>DO NOT USE THIS!</b>
     *
     * <p>This is a method necessary for linking a bot's response messages
     * to their corresponding call message ID.
     * <br><b>Using this anywhere in your code can and will break your bot.</b>
     *
     * @param  callId
     *         The ID of the call Message
     * @param  message
     *         The Message to link to the ID
     */
    public void linkIds(long callId, Message message)
    {
        synchronized (linkMap)
        {
            Set<Message> stored = linkMap.get(callId);
            if(stored != null)
                stored.add(message);
            else
            {
                stored = new HashSet<>();
                stored.add(message);
                linkMap.add(callId, stored);
            }
        }
    }
}

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
package com.jagrosh.jdautilities.utils;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A series of query based utilities for finding entities, either globally across all accessible
 * {@link net.dv8tion.jda.core.entities.Guild Guild}s, or locally to a specified Guild.
 *
 * <p>All methods use a similar priority hierarchy and return an immutable {@link java.util.List List} based on the results.
 * <br>The hierarchy is as follows:
 * <ul>
 *     <li>Special Cases: Specifics of these are described per individual method documentation.
 *     <br>Note that successful results from these are typically
 *     {@link java.util.Collections#singletonList(Object) a singleton list}.</li>
 *     <li>Direct ID: Query is a number with 17 or more digits, resembling an
 *     {@link net.dv8tion.jda.core.entities.ISnowflake Snowflake} ID.</li>
 *     <li>Exact Match: Query provided is an exact match (case sensitive and complete) to one or more entities.</li>
 *     <li>Wrong Case: Query provided is a case-insensitive, but exact, match to the entirety of one or more entities.</li>
 *     <li>Starting With: Query provided is an case-insensitive match to the beginning of one or more entities.</li>
 *     <li>Contains: Query provided is a case-insensitive match to a part of one or more entities.</li>
 * </ul>
 * All queries return the highest List in this hierarchy that contains one or more entities, and only of these
 * kind of results (IE: the "exact" list will never contain any results from a successful "starting with" match,
 * unless by chance they could technically be the same result).<p>
 *
 * <b>Shard Manager Usage</b>
 *
 * <br>Methods that query an instance of {@link net.dv8tion.jda.core.JDA JDA} always have two implementations:
 * <ul>
 *     <li><b>Global:</b> Queries a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} if one
 *                        is available, or JDA if one is not.</li>
 *
 *     <li><b>Shard:</b>  Always queries the provided instance, and never a ShardManager, even if one
 *                        is available.</li>
 * </ul>
 *
 * <p>Many of these utilities were inspired by and ported to JDA 3.X from
 * <a href="https://github.com/jagrosh/Spectra/blob/master/src/spectra/utils/FinderUtil.java">Spectra's FinderUtil</a>
 * originally written by <a href="https://github.com/jagrosh/">jagrosh</a> in 2.X.
 *
 * @since  1.3
 * @author Kaidan Gustave
 */
@SuppressWarnings("Duplicates")
public class FinderUtil
{
    public final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
    public final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    public final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
    public final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
    public final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID
    public final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>");

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for {@link net.dv8tion.jda.core.entities.User User}s.<p>
     *
     * If a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available this will query across that
     * instead of the JDA instance.
     *
     * <p>The following special cases are applied in order of listing before the standard search is done:
     * <ul>
     *     <li>User Mention: Query provided matches an @user mention (more specifically {@literal <@userID>}).</li>
     *     <li>Full User Reference: Query provided matches a full Username#XXXX reference.
     *     <br><b>NOTE:</b> this can return a list with more than one entity.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Users found by the query from the provided JDA instance.
     */
    public static List<User> findUsers(String query, JDA jda)
    {
        return jdaUserSearch(query, jda, true);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for {@link net.dv8tion.jda.core.entities.User User}s.<p>
     *
     * This only queries the instance of JDA, regardless of whether or not a
     * {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available.
     *
     * <p>The following special cases are applied in order of listing before the standard search is done:
     * <ul>
     *     <li>User Mention: Query provided matches an @user mention (more specifically {@literal <@userID>}).</li>
     *     <li>Full User Reference: Query provided matches a full Username#XXXX reference.
     *     <br><b>NOTE:</b> this can return a list with more than one entity.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Users found by the query from the provided JDA instance.
     */
    public static List<User> findShardUsers(String query, JDA jda)
    {
        return jdaUserSearch(query, jda, false);
    }

    private static List<User> jdaUserSearch(String query, JDA jda, boolean useShardManager)
    {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        ShardManager manager = useShardManager? jda.asBot().getShardManager() : null;

        if(userMention.matches())
        {
            User user = manager != null? manager.getUserById(userMention.group(1)) :
                        jda.getUserById(userMention.group(1));
            if(user!=null)
                return Collections.singletonList(user);
        }
        else if(fullRefMatch.matches())
        {
            String lowerName = fullRefMatch.group(1).toLowerCase();
            String discrim = fullRefMatch.group(2);
            List<User> users = (manager != null ? manager.getUserCache() : jda.getUserCache())
                .stream().filter(user -> user.getName().toLowerCase().equals(lowerName)
                                         && user.getDiscriminator().equals(discrim))
                .collect(Collectors.toList());
            if(!users.isEmpty())
                return users;
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            User user = (manager != null? manager.getUserById(query) : jda.getUserById(query));
            if(user!=null)
                return Collections.singletonList(user);
        }

        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        (manager != null? manager.getUserCache() : jda.getUserCache()).forEach(user -> {
            String name = user.getName();
            if(name.equals(query))
                exact.add(user);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(user);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(user);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(user);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for a banned {@link net.dv8tion.jda.core.entities.User
     * User}.
     *
     * <p>The following special cases are applied in order of listing before the standard search is done:
     * <ul>
     *     <li>User Mention: Query provided matches an @user mention (more specifically {@literal <@userID>}).</li>
     *     <li>Full User Reference: Query provided matches a full Username#XXXX reference.
     *     <br><b>NOTE:</b> this can return a list with more than one entity.</li>
     * </ul>
     *
     * <b>WARNING</b>
     *
     * Unlike the other finder methods, this one has two very unique features that set it apart from the rest:
     * <ul>
     *     <li><b>1)</b> In order to get a list of bans that is usable, this method initial retrieves it by usage of
     *     {@link net.dv8tion.jda.core.requests.RestAction#complete() Guild#getBans().complete()}. Because of this,
     *     as would be the same expected effect from the other utility methods, this will block the thread it is called
     *     in. The difference, however, comes in that this method may have slight variations in return speed, especially
     *     when put under higher usage over a shorter period of time.</li>
     *     <li><b>2) This method can return {@code null}</b> if and only if an {@link java.lang.Exception Exception} is
     *     thrown while initially getting banned Users via {@link net.dv8tion.jda.core.entities.Guild#getBans()
     *     Guild#getBans()}.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search for banned Users from
     *
     * @return A possibly-empty {@link java.util.List List} of Users found by the query from the provided JDA instance,
     *         or {@code null} if an {@link java.lang.Exception Exception} is thrown while initially getting banned Users.
     *
     * @see    net.dv8tion.jda.core.entities.Guild#getBans() Guild#getBans()
     */
    public static List<User> findBannedUsers(String query, Guild guild)
    {
        List<User> bans;
        try {
            bans = guild.getBans().complete();
        } catch(Exception e) {
            return null;
        }
        String discrim = null;
        Matcher userMention = USER_MENTION.matcher(query);
        if(userMention.matches())
        {
            String id = userMention.group(1);
            User user = guild.getJDA().getUserById(id);
            if(user != null && bans.contains(user))
                return Collections.singletonList(user);
            for(User u : bans)
                if(u.getId().equals(id))
                    return Collections.singletonList(u);
        }
        else if(FULL_USER_REF.matcher(query).matches())
        {
            discrim = query.substring(query.length()-4);
            query = query.substring(0,query.length()-5).trim();
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            User user = guild.getJDA().getUserById(query);
            if(user != null && bans.contains(user))
                return Collections.singletonList(user);
            for(User u : bans)
                if(u.getId().equals(query))
                    return Collections.singletonList(u);
        }
        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for(User u: bans)
        {
            // If a discrim is specified then we skip all users without it.
            if(discrim!=null && !u.getDiscriminator().equals(discrim))
                continue;

            if(u.getName().equals(query))
                exact.add(u);
            else if(exact.isEmpty() && u.getName().equalsIgnoreCase(query))
                wrongcase.add(u);
            else if(wrongcase.isEmpty() && u.getName().toLowerCase().startsWith(lowerQuery))
                startswith.add(u);
            else if(startswith.isEmpty() && u.getName().toLowerCase().contains(lowerQuery))
                contains.add(u);
        }
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.Member Member}s.
     *
     * <p>The following special cases are applied in order of listing before the standard search is done:
     * <ul>
     *     <li>User Mention: Query provided matches an @user mention (more specifically {@literal <@userID> or <@!userID>}).</li>
     *     <li>Full User Reference: Query provided matches a full Username#XXXX reference.
     *     <br><b>NOTE:</b> this can return a list with more than one entity.</li>
     * </ul>
     *
     * <p>Unlike {@link com.jagrosh.jdautilities.utils.FinderUtil#findUsers(String, JDA) FinderUtil.findUsers(String, JDA)},
     * this method queries based on two different names: user name and effective name (excluding special cases in which it
     * queries solely based on user name).
     * <br>Each standard check looks at the user name, then the member name, and if either one's criteria is met the Member
     * is added to the returned list. This is important to note, because the returned list may contain exact matches for
     * User's name as well as exact matches for a Member's effective name, with nothing guaranteeing the returns will be
     * exclusively containing matches for one or the other.
     * <br>Information on effective name can be found in {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()
     * Member#getEffectiveName()}.
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly empty {@link java.util.List List} of Members found by the query from the provided Guild.
     */
    public static List<Member> findMembers(String query, Guild guild)
    {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);
        if(userMention.matches())
        {
            Member member = guild.getMemberById(userMention.group(1));
            if(member!=null)
                return Collections.singletonList(member);
        }
        else if(fullRefMatch.matches())
        {
            String lowerName = fullRefMatch.group(1).toLowerCase();
            String discrim = fullRefMatch.group(2);
            List<Member> members = guild.getMemberCache().stream()
                    .filter(member -> member.getUser().getName().toLowerCase().equals(lowerName)
                            && member.getUser().getDiscriminator().equals(discrim))
                    .collect(Collectors.toList());
            if(!members.isEmpty())
                return members;
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            Member member = guild.getMemberById(query);
            if(member!=null)
                return Collections.singletonList(member);
        }
        ArrayList<Member> exact = new ArrayList<>();
        ArrayList<Member> wrongcase = new ArrayList<>();
        ArrayList<Member> startswith = new ArrayList<>();
        ArrayList<Member> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getMemberCache().forEach(member -> {
            String name = member.getUser().getName();
            String effName = member.getEffectiveName();
            if(name.equals(query) || effName.equals(query))
                exact.add(member);
            else if((name.equalsIgnoreCase(query) || effName.equalsIgnoreCase(query)) && exact.isEmpty())
                wrongcase.add(member);
            else if((name.toLowerCase().startsWith(lowerquery) || effName.toLowerCase().startsWith(lowerquery)) && wrongcase.isEmpty())
                startswith.add(member);
            else if((name.toLowerCase().contains(lowerquery) || effName.toLowerCase().contains(lowerquery)) && startswith.isEmpty())
                contains.add(member);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}s.<p>
     *
     * If a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available this will query across that
     * instead of the JDA instance.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Channel Mention: Query provided matches a #channel mention (more specifically {@literal <#channelID>})</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of TextChannels found by the query from the provided JDA instance.
     */
    public static List<TextChannel> findTextChannels(String query, JDA jda)
    {
        return jdaTextChannelSearch(query, jda, true);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}s.<p>
     *
     * This only queries the instance of JDA, regardless of whether or not a
     * {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Channel Mention: Query provided matches a #channel mention (more specifically {@literal <#channelID>})</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of TextChannels found by the query from the provided JDA instance.
     */
    public static List<TextChannel> findShardTextChannels(String query, JDA jda)
    {
        return jdaTextChannelSearch(query, jda, false);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}s.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Channel Mention: Query provided matches a #channel mention (more specifically {@literal <#channelID>})</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly-empty {@link java.util.List List} of TextChannels found by the query from the provided Guild.
     */
    public static List<TextChannel> findTextChannels(String query, Guild guild)
    {
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        if(channelMention.matches())
        {
            TextChannel tc = guild.getTextChannelById(channelMention.group(1));
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            TextChannel tc = guild.getTextChannelById(query);
            if(tc!=null)
                return Collections.singletonList(tc);
        }

        return genericTextChannelSearch(query, guild.getTextChannelCache());
    }

    private static List<TextChannel> jdaTextChannelSearch(String query, JDA jda, boolean useShardManager)
    {
        Matcher channelMention = CHANNEL_MENTION.matcher(query);

        ShardManager manager = useShardManager? jda.asBot().getShardManager() : null;

        if(channelMention.matches())
        {
            TextChannel tc = manager != null? manager.getTextChannelById(channelMention.group(1)) :
                jda.getTextChannelById(channelMention.group(1));
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            TextChannel tc = manager != null? manager.getTextChannelById(query) : jda.getTextChannelById(query);
            if(tc!=null)
                return Collections.singletonList(tc);
        }

        return genericTextChannelSearch(query, manager != null? manager.getTextChannelCache() : jda.getTextChannelCache());
    }

    private static List<TextChannel> genericTextChannelSearch(String query, SnowflakeCacheView<TextChannel> cache)
    {
        ArrayList<TextChannel> exact = new ArrayList<>();
        ArrayList<TextChannel> wrongcase = new ArrayList<>();
        ArrayList<TextChannel> startswith = new ArrayList<>();
        ArrayList<TextChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach((tc) -> {
            String name = tc.getName();
            if(name.equals(query))
                exact.add(tc);
            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(tc);
            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(tc);
            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(tc);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}s.<p>
     *
     * If a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available this will query across that
     * instead of the JDA instance.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of VoiceChannels found by the query from the provided JDA instance.
     */
    public static List<VoiceChannel> findVoiceChannels(String query, JDA jda)
    {
        return jdaVoiceChannelSearch(query, jda, true);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}s.<p>
     *
     * This only queries the instance of JDA, regardless of whether or not a
     * {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of VoiceChannels found by the query from the provided JDA instance.
     */
    public static List<VoiceChannel> findShardVoiceChannels(String query, JDA jda)
    {
        return jdaVoiceChannelSearch(query, jda, false);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}s.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly-empty {@link java.util.List List} of VoiceChannels found by the query from the provided Guild.
     */
    public static List<VoiceChannel> findVoiceChannels(String query, Guild guild)
    {
        if(DISCORD_ID.matcher(query).matches())
        {
            VoiceChannel vc = guild.getVoiceChannelById(query);
            if(vc!=null)
                return Collections.singletonList(vc);
        }
        return genericVoiceChannelSearch(query, guild.getVoiceChannelCache());
    }

    private static List<VoiceChannel> jdaVoiceChannelSearch(String query, JDA jda, boolean useShardManager)
    {
        ShardManager manager = useShardManager? jda.asBot().getShardManager() : null;

        if(DISCORD_ID.matcher(query).matches())
        {
            VoiceChannel vc = manager != null? manager.getVoiceChannelById(query) : jda.getVoiceChannelById(query);
            if(vc!=null)
                return Collections.singletonList(vc);
        }

        return genericVoiceChannelSearch(query, manager != null? manager.getVoiceChannelCache() : jda.getVoiceChannelCache());
    }

    private static List<VoiceChannel> genericVoiceChannelSearch(String query, SnowflakeCacheView<VoiceChannel> cache)
    {
        ArrayList<VoiceChannel> exact = new ArrayList<>();
        ArrayList<VoiceChannel> wrongcase = new ArrayList<>();
        ArrayList<VoiceChannel> startswith = new ArrayList<>();
        ArrayList<VoiceChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach((vc) -> {
            String name = vc.getName();
            if(name.equals(query))
                exact.add(vc);
            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(vc);
            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(vc);
            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(vc);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.Category Categories}.<p>
     *
     * If a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available this will query across that
     * instead of the JDA instance.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided JDA instance.
     */
    public static List<Category> findCategories(String query, JDA jda)
    {
        return jdaCategorySearch(query, jda, true);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.Category Categories}.<p>
     *
     * This only queries the instance of JDA, regardless of whether or not a
     * {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided JDA instance.
     */
    public static List<Category> findShardCategories(String query, JDA jda)
    {
        return jdaCategorySearch(query, jda, false);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * {@link net.dv8tion.jda.core.entities.Category Categories}.
     *
     * <p>The standard search does not follow any special cases.
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided Guild.
     */
    public static List<Category> findCategories(String query, Guild guild)
    {
        if(DISCORD_ID.matcher(query).matches())
        {
            Category cat = guild.getCategoryById(query);
            if(cat != null)
                return Collections.singletonList(cat);
        }

        return genericCategorySearch(query, guild.getCategoryCache());
    }

    private static List<Category> jdaCategorySearch(String query, JDA jda, boolean useShardManager)
    {
        ShardManager manager = useShardManager? jda.asBot().getShardManager() : null;

        if(DISCORD_ID.matcher(query).matches())
        {
            Category cat = manager != null? manager.getCategoryById(query) : jda.getCategoryById(query);
            if(cat != null)
                return Collections.singletonList(cat);
        }

        return genericCategorySearch(query, jda.getCategoryCache());
    }

    private static List<Category> genericCategorySearch(String query, SnowflakeCacheView<Category> cache)
    {
        ArrayList<Category> exact = new ArrayList<>();
        ArrayList<Category> wrongcase = new ArrayList<>();
        ArrayList<Category> startswith = new ArrayList<>();
        ArrayList<Category> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach(cat -> {
            String name = cat.getName();
            if(name.equals(query))
                exact.add(cat);
            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(cat);
            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(cat);
            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(cat);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.Role Role}s.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Role Mention: Query provided matches a @role mention (more specifically {@literal <@&roleID>})</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Roles found by the query from the provided Guild.
     */
    public static List<Role> findRoles(String query, Guild guild)
    {
        Matcher roleMention = ROLE_MENTION.matcher(query);
        if(roleMention.matches())
        {
            Role role = guild.getRoleById(roleMention.group(1));
            if(role!=null && role.isMentionable())
                return Collections.singletonList(role);
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            Role role = guild.getRoleById(query);
            if(role!=null)
                return Collections.singletonList(role);
        }
        ArrayList<Role> exact = new ArrayList<>();
        ArrayList<Role> wrongcase = new ArrayList<>();
        ArrayList<Role> startswith = new ArrayList<>();
        ArrayList<Role> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getRoleCache().forEach((role) -> {
            String name = role.getName();
            if(name.equals(query))
                exact.add(role);
            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(role);
            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(role);
            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(role);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.Emote Emote}s.<p>
     *
     * If a {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available this will query across that
     * instead of the JDA instance.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
     *     Emote and that the {@link net.dv8tion.jda.core.entities.Emote#getName() name} of the Emote is equal to the
     *     name found in the query.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided JDA instance.
     */
    public static List<Emote> findEmotes(String query, JDA jda)
    {
        return jdaFindEmotes(query, jda, true);
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for
     * {@link net.dv8tion.jda.core.entities.Emote Emote}s.<p>
     *
     * This only queries the instance of JDA, regardless of whether or not a
     * {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} is available.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
     *     Emote and that the {@link net.dv8tion.jda.core.entities.Emote#getName() name} of the Emote is equal to the
     *     name found in the query.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  jda
     *         The instance of JDA to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided JDA instance.
     */
    public static List<Emote> findShardEmotes(String query, JDA jda)
    {
        return jdaFindEmotes(query, jda, false);
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * {@link net.dv8tion.jda.core.entities.Emote Emote}s.
     *
     * <p>The following special case is applied before the standard search is done:
     * <ul>
     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
     *     Emote and that the {@link net.dv8tion.jda.core.entities.Emote#getName() name} of the Emote is equal to the
     *     name found in the query.</li>
     * </ul>
     *
     * @param  query
     *         The String query to search by
     * @param  guild
     *         The Guild to search from
     *
     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided Guild.
     */
    public static List<Emote> findEmotes(String query, Guild guild)
    {
        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);
        if(DISCORD_ID.matcher(query).matches())
        {
            Emote emote = guild.getEmoteById(query);
            if(emote != null)
                return Collections.singletonList(emote);
        }
        else if(mentionMatcher.matches())
        {
            String emoteName = mentionMatcher.group(1);
            String emoteId = mentionMatcher.group(2);
            Emote emote = guild.getEmoteById(emoteId);
            if(emote != null && emote.getName().equals(emoteName))
                return Collections.singletonList(emote);
        }

        return genericEmoteSearch(query, guild.getEmoteCache());
    }

    private static List<Emote> jdaFindEmotes(String query, JDA jda, boolean useShardManager)
    {
        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);

        ShardManager manager = useShardManager? jda.asBot().getShardManager() : null;

        if(DISCORD_ID.matcher(query).matches())
        {
            Emote emote = manager != null? manager.getEmoteById(query) : jda.getEmoteById(query);
            if(emote != null)
                return Collections.singletonList(emote);
        }
        else if(mentionMatcher.matches())
        {
            String emoteName = mentionMatcher.group(1);
            String emoteId = mentionMatcher.group(2);
            Emote emote = manager != null? manager.getEmoteById(emoteId) : jda.getEmoteById(emoteId);
            if(emote != null && emote.getName().equals(emoteName))
                return Collections.singletonList(emote);
        }

        return genericEmoteSearch(query, jda.getEmoteCache());
    }

    private static List<Emote> genericEmoteSearch(String query, SnowflakeCacheView<Emote> cache)
    {
        ArrayList<Emote> exact = new ArrayList<>();
        ArrayList<Emote> wrongcase = new ArrayList<>();
        ArrayList<Emote> startswith = new ArrayList<>();
        ArrayList<Emote> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach(emote -> {
            String name = emote.getName();
            if(name.equals(query))
                exact.add(emote);
            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(emote);
            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(emote);
            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(emote);
        });
        if(!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if(!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if(!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    // Prevent instantiation
    private FinderUtil(){}
}

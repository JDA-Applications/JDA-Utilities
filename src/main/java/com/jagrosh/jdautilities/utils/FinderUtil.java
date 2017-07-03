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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A series of query based utilities for finding entities, either globally across all accessible {@link
 * net.dv8tion.jda.core.entities.Guild Guild}s, or locally to a specified Guild.
 *
 * <p>All methods use a similar priority hierarchy and return a {@link java.util.List List} based on the results.
 * <br>The hierarchy is as follows:
 * <ul>
 *     <li>Special Cases: Specifics of these are described per individual method documentation.
 *     <br>Note that successful results from these are typically {@link java.util.Collections#singletonList(Object)
 *     a singleton list}.</li>
 *     <li>Direct ID: Query is a number with 17 or more digits, resembling an {@link net.dv8tion.jda.core.entities.ISnowflake
 *     ISnowflake} ID.</li>
 *     <li>Exact Match: Query provided is an exact match (case sensitive and complete) to one or more entities.</li>
 *     <li>Wrong Case: Query provided is a case-insensitive, but exact, match to the entirety of one or more entities.</li>
 *     <li>Starting With: Query provided is an case-insensitive match to the beginning of one or more entities.</li>
 *     <li>Contains: Query provided is a case-insensitive match to a part of one or more entities.</li>
 * </ul>
 * All queries return the highest List in this hierarchy that contains one or more entities, and only of these
 * kind of results (IE: the "exact" list will never contain any results from a successful "starting with" match,
 * unless by chance they could technically be the same result).
 *
 * <p>All of these utilities were inspired by and ported to JDA 3.X from
 * <a href="https://github.com/jagrosh/Spectra/blob/master/src/spectra/utils/FinderUtil.java">Spectra's FinderUtil</a>
 * originally written by <a href="https://github.com/jagrosh/">jagrosh</a> in 2.X.
 *
 * @since  1.3
 * @author Kaidan Gustave
 */
public class FinderUtil {

    private final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
    private final static Pattern FULL_USER_REF = Pattern.compile("(.{2,32})\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    private final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
    private final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
    private final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for {@link net.dv8tion.jda.core.entities.User User}s.
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
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);
        if(userMention.matches())
        {
            User user = jda.getUserById(userMention.replaceAll("$1"));
            if(user!=null)
                return Collections.singletonList(user);
        }
        else if(fullRefMatch.matches())
        {
            String lowerName = fullRefMatch.replaceAll("$1").toLowerCase();
            String discrim = fullRefMatch.replaceAll("$2");
            List<User> users = jda.getUsers().stream()
                    .filter(user -> user.getName().toLowerCase().equals(lowerName)
                            && user.getDiscriminator().equals(discrim))
                    .collect(Collectors.toList());
            if(!users.isEmpty())
                return users;
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            User user = jda.getUserById(query);
            if(user!=null)
                return Collections.singletonList(user);
        }
        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        jda.getUsers().stream().forEach(user -> {
            String name = user.getName();
            if(name.equals(lowerquery))
                exact.add(user);
            else if (name.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(user);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(user);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(user);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.Member Member}s.
     *
     * <p>Unlike {@link com.jagrosh.jdautilities.utils.FinderUtil#findUsers(String, JDA) FinderUtil.findUsers(String, JDA)}, this method
     * queries based on effective name (excluding special cases).
     * <br>Information on effective name can be found in {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()
     * Member#getEffectiveName()}
     *
     * <p>The following special cases are applied in order of listing before the standard search is done:
     * <ul>
     *     <li>User Mention: Query provided matches an @user mention (more specifically {@literal <@userID> or <@!userID>}).</li>
     *     <li>Full User Reference: Query provided matches a full Username#XXXX reference.
     *     <br><b>NOTE:</b> this can return a list with more than one entity.</li>
     * </ul>
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
            Member member = guild.getMemberById(userMention.replaceAll("$1"));
            if(member!=null)
                return Collections.singletonList(member);
        }
        else if(fullRefMatch.matches())
        {
            String lowerName = fullRefMatch.replaceAll("$1").toLowerCase();
            String discrim = fullRefMatch.replaceAll("$2");
            List<Member> members = guild.getMembers().stream()
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
        guild.getMembers().stream().forEach(member -> {
            String effName = member.getEffectiveName();
            if(effName.equals(lowerquery))
                exact.add(member);
            else if (effName.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(member);
            else if (effName.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(member);
            else if (effName.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(member);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for {@link net.dv8tion.jda.core.entities.TextChannel
     * TextChannel}s.
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
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        if(channelMention.matches())
        {
            TextChannel tc = jda.getTextChannelById(channelMention.replaceAll("$1"));
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            TextChannel tc = jda.getTextChannelById(query);
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        ArrayList<TextChannel> exact = new ArrayList<>();
        ArrayList<TextChannel> wrongcase = new ArrayList<>();
        ArrayList<TextChannel> startswith = new ArrayList<>();
        ArrayList<TextChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        jda.getTextChannels().stream().forEach((tc) -> {
            String name = tc.getName();
            if(name.equals(lowerquery))
                exact.add(tc);
            else if (name.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(tc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(tc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(tc);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.TextChannel
     * TextChannel}s.
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
            TextChannel tc = guild.getTextChannelById(channelMention.replaceAll("$1"));
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        else if(DISCORD_ID.matcher(query).matches())
        {
            TextChannel tc = guild.getTextChannelById(query);
            if(tc!=null)
                return Collections.singletonList(tc);
        }
        ArrayList<TextChannel> exact = new ArrayList<>();
        ArrayList<TextChannel> wrongcase = new ArrayList<>();
        ArrayList<TextChannel> startswith = new ArrayList<>();
        ArrayList<TextChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getTextChannels().stream().forEach((tc) -> {
            String name = tc.getName();
            if(name.equals(lowerquery))
                exact.add(tc);
            else if (name.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(tc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(tc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(tc);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided instance of {@link net.dv8tion.jda.core.JDA JDA} for {@link net.dv8tion.jda.core.entities.VoiceChannel
     * VoiceChannel}s.
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
        if(DISCORD_ID.matcher(query).matches())
        {
            VoiceChannel vc = jda.getVoiceChannelById(query);
            if(vc!=null)
                return Collections.singletonList(vc);
        }
        ArrayList<VoiceChannel> exact = new ArrayList<>();
        ArrayList<VoiceChannel> wrongcase = new ArrayList<>();
        ArrayList<VoiceChannel> startswith = new ArrayList<>();
        ArrayList<VoiceChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        jda.getVoiceChannels().stream().forEach((vc) -> {
            String name = vc.getName();
            if(name.equals(lowerquery))
                exact.add(vc);
            else if (name.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(vc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(vc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(vc);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.VoiceChannel
     * VoiceChannel}s.
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
        ArrayList<VoiceChannel> exact = new ArrayList<>();
        ArrayList<VoiceChannel> wrongcase = new ArrayList<>();
        ArrayList<VoiceChannel> startswith = new ArrayList<>();
        ArrayList<VoiceChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getVoiceChannels().stream().forEach((vc) -> {
            String name = vc.getName();
            if(name.equals(lowerquery))
                exact.add(vc);
            else if (name.equalsIgnoreCase(lowerquery) && exact.isEmpty())
                wrongcase.add(vc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(vc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(vc);
        });
        if(!exact.isEmpty())
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    /**
     * Queries a provided {@link net.dv8tion.jda.core.entities.Guild Guild} for {@link net.dv8tion.jda.core.entities.Role Role}s.
     *
     * <p>The following special case is applied in order of listing before the standard search is done:
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
            Role role = guild.getRoleById(roleMention.replaceAll("$1"));
            if(role!=null)
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
        guild.getRoles().stream().forEach((role) -> {
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
            return exact;
        if(!wrongcase.isEmpty())
            return wrongcase;
        if(!startswith.isEmpty())
            return startswith;
        return contains;
    }

    // Prevent instantiation
    private FinderUtil(){}
}
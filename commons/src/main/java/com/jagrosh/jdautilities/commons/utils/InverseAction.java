package com.jagrosh.jdautilities.commons.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.*;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.self.*;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class meant to serve as a way to rollback certain events where it makes sense to.
 *
 * <p>
 * All methods are overloaded static factory methods, always returning a {@link net.dv8tion.jda.api.requests.RestAction RestAction}
 * meant to "undo" the given event. Note that this will only be possible for events in which it makes sense.
 * In events where something is posted, the inverse would be another request to remove said post, for example.
 * <p>
 * Also, since it's a RestAction that simply makes an attempt at the inverse, it's not always possible due to multiple
 * factors, such as permissions, etc. For example, trying to inverse a {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceievedEvent} in a channel with no permission
 * to remove messages will fail
 * </p>
 * <br>
 * <p>
 * Events fired when something is "created" like a {@link net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent PermissionOverrideCreateEvent}
 * can usually be logically inverted, by removing what was created.
 * <p>
 * Events fired for the deletion of something are a different story. Some cases are logical, but many are not, like the
 * removal of a text channel. Cases where the complete undoing of the event cannot be guaranteed are not implemented
 * </p>
 *
 * @author HydroPage90
 */
public final class InverseAction
{
    /**
     * @return An attempt to change the category's position back
     */
    public static ChannelManager of(CategoryUpdatePositionEvent event)
    {
        ChannelManager manager = event.getCategory().getManager();
        int oldPosition = event.getOldPosition();

        return manager.setPosition(oldPosition);
    }

    /**
     * @return An attempt to change the category's name back
     */
    public static ChannelManager of(CategoryUpdateNameEvent event)
    {
        ChannelManager manager = event.getCategory().getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    /**
     * @return An attempt to remove said category
     */
    public static AuditableRestAction<Void> of(CategoryCreateEvent event)
    {
        return event.getCategory().delete();
    }

    /**
     * @return An attempt to remove said invite
     */
    public static AuditableRestAction<Void> of(GuildInviteCreateEvent event)
    {
        return event.getInvite().delete();
    }

    /**
     * @return An attempt to leave said guild
     */
    public static RestAction<?> of(GuildJoinEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to take said roles away
     */
    public static AuditableRestAction<Void> of(GuildMemberRoleAddEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        List<Role> addedRoles = event.getRoles();

        return guild.modifyMemberRoles(member, null, addedRoles);
    }

    /**
     * @return An attempt to give the member the roles back
     */
    public static AuditableRestAction<Void> of(GuildMemberRoleRemoveEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        List<Role> rolesRemoved = event.getRoles();

        return guild.modifyMemberRoles(member, rolesRemoved, null);
    }

    /**
     * @return An attempt to change the member's nickname back
     */
    public static AuditableRestAction<Void> of(GuildMemberUpdateNicknameEvent event)
    {
        Member member = event.getMember();
        String oldNick = event.getOldNickname();

        return member.modifyNickname(oldNick);
    }

    //TODO Look into all the GenericGuildUpdateEvents. There are a lot

    /**
     * @return An attempt to kick said member
     */
    public static AuditableRestAction<Void> of(GuildMemberJoinEvent event)
    {
        return event.getMember().kick();
    }

    /**
     * @return An attempt to undeafen if they were deafened, or an attempt to deafen if they were undeafened
     */
    public static RestAction<Void> of(GuildVoiceGuildDeafenEvent event)
    {
        return event.getGuild().leave();
    }

    /**
     * @return An attempt to kick them from the channel
     */
    public static RestAction<Void> of(GuildVoiceJoinEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        return guild.kickVoiceMember(member);
    }

    /**
     * @return An attempt to move them back to where they just were
     */
    public static RestAction<Void> of(GuildVoiceMoveEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        VoiceChannel previous = event.getOldValue();

        return guild.moveVoiceMember(member, previous);
    }

    /**
     * @return An attempt to unmute if they were muted, or an attempt to mute if they were unmuted
     */
    public static AuditableRestAction<Void> of(GuildVoiceGuildMuteEvent event)
    {
        Member member = event.getMember();
        boolean action = event.isGuildMuted();

        return member.mute(!action);
    }

    /**
     * @return An attempt to remove said override
     */
    public static AuditableRestAction<Void> of(PermissionOverrideCreateEvent event)
    {
        return event.getPermissionOverride().delete();
    }

    /**
     * @return An attempt to add the override back
     */
    public static PermissionOverrideAction of(PermissionOverrideDeleteEvent event)
    {
        PermissionOverride deleted = event.getPermissionOverride();
        EnumSet<Permission> allowed = deleted.getAllowed();
        EnumSet<Permission> denied = deleted.getDenied();

        return event.getChannel()
                    .createPermissionOverride(event.getPermissionHolder())
                    .setPermissions(allowed, denied);
    }

    /**
     * @return An attempt to set rules to what they just were
     */
    public static PermissionOverrideAction of(PermissionOverrideUpdateEvent event)
    {
        PermissionOverride updated = event.getPermissionOverride();
        EnumSet<Permission> allow = event.getOldAllow();
        EnumSet<Permission> deny = event.getOldDeny();

        return updated.getManager()
                      .setAllow(allow).setDeny(deny);
    }

    /**
     * @return An attempt to remove said emote
     */
    public static AuditableRestAction<Void> of(EmoteAddedEvent event)
    {
        return event.getEmote().delete();
    }

    /**
     * @return An attempt to change the name back to what it just was
     */
    public static EmoteManager of(EmoteUpdateNameEvent event)
    {
        Emote emote = event.getEmote();
        EmoteManager manager = emote.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    /**
     * @return An attempt to change the roles to what they just were
     */
    public static EmoteManager of(EmoteUpdateRolesEvent event)
    {
        Emote emote = event.getEmote();
        EmoteManager manager = emote.getManager();
        HashSet<Role> oldRoles = new HashSet<>(event.getOldRoles());

        return manager.setRoles(oldRoles);
    }

    /**
     * @return An attempt to remove said role
     */
    public static RestAction<?> of(RoleCreateEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to add the role back
     */
    public static RestAction<?> of(RoleDeleteEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to make the role unmentionable again
     */
    public static RestAction<?> of(RoleUpdateMentionableEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to move the role back to where it just was
     */
    public static RestAction<?> of(RoleUpdatePositionEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the role's permissions back
     */
    public static RestAction<?> of(RoleUpdatePermissionsEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the role's name back
     */
    public static RestAction<?> of(RoleUpdateNameEvent event)
    {
        return null;
    }

    //TODO What the hell is a hoisted role? Lmao, look at that later

    /**
     * @return An attempt to change the role's color back to what it just was
     */
    public static RestAction<?> of(RoleUpdateColorEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said channel
     */
    public static RestAction<?> of(PrivateChannelCreateEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to make the channel again
     */
    public static RestAction<?> of(VoiceChannelDeleteEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said channel
     */
    public static RestAction<?> of(VoiceChannelCreateEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the voice channel's name back
     */
    public static RestAction<?> of(VoiceChannelUpdateNameEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the voice channel's parent back
     */
    public static RestAction<?> of(VoiceChannelUpdateParentEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the voice channel's position back
     */
    public static RestAction<?> of(VoiceChannelUpdatePositionEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the voice channel's bitrate back
     */
    public static RestAction<?> of(VoiceChannelUpdateBitrateEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the voice channel's user limit back
     */
    public static RestAction<?> of(VoiceChannelUpdateUserLimitEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said channel
     */
    public static RestAction<?> of(TextChannelCreateEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the text channel's topic back
     */
    public static RestAction<?> of(TextChannelUpdateTopicEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the text channel's name back
     */
    public static RestAction<?> of(TextChannelUpdateNameEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the text channel's slowmode value back
     */
    public static RestAction<?> of(TextChannelUpdateSlowmodeEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to move the text channel back
     */
    public static RestAction<?> of(TextChannelUpdatePositionEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the text channel's NSFW state back
     */
    public static RestAction<?> of(TextChannelUpdateNSFWEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the text channel's parent back
     */
    public static RestAction<?> of(TextChannelUpdateParentEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said reaction
     */
    public static RestAction<?> of(MessageReactionAddEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said message
     */
    public static RestAction<?> of(MessageReceivedEvent event)
    {
        return null;
    }

    // I'm not really sure what to do about store channels.

    /**
     * @return An attempt to move the store channel back
     */
    public static RestAction<?> of(StoreChannelUpdatePositionEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to change the store channel's name back
     */
    public static RestAction<?> of(StoreChannelUpdateNameEvent event)
    {
        return null;
    }

    /**
     * @return An attempt to remove said store channel
     */
    public static RestAction<?> of(StoreChannelCreateEvent event)
    {
        return null;
    }
}

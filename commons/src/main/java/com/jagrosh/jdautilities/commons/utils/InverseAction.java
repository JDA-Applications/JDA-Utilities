package com.jagrosh.jdautilities.commons.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
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
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
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
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.requests.restaction.order.OrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

/**
 * A utility class meant to serve as a way to rollback certain events where it makes sense to.
 *
 * <p>
 * All methods are overloaded static factory methods, always returning a {@link net.dv8tion.jda.api.requests.RestAction RestAction<?>}
 * meant to "undo" the given event. Note that this will only be possible for events in which it makes sense.
 * In events where something is posted, the inverse would be another request to remove said post, for example.
 * </p>
 * <br>
 * <p>
 * Also, since it's a RestAction that simply makes an attempt at the inverse, it's not always possible due to multiple
 * factors, such as permissions, etc. For example, trying to invert a {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent} in a channel with no permission
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
 * <br>
 * <p>
 * Keep in mind that inverting the deletion of something requires for a new object to be created with the same values.
 * But doing this can't possibly copy information such as the ID of the deleted object or undo effects like everyone losing
 * a role that is deleted
 *
 * @author HydroPage90
 */
public final class InverseAction
{
    public static RestAction<?> of(GenericEvent event)
    {
        if (event instanceof CategoryUpdatePositionEvent)
            return inverse((CategoryUpdatePositionEvent) event);

        else if (event instanceof CategoryUpdateNameEvent)
            return inverse((CategoryUpdateNameEvent) event);

        else if (event instanceof CategoryCreateEvent)
            return inverse((CategoryCreateEvent) event);

        else if (event instanceof GuildInviteCreateEvent)
            return inverse((GuildInviteCreateEvent) event);

        else if (event instanceof GuildJoinEvent)
            return inverse((GuildJoinEvent) event);

        else if (event instanceof GuildBanEvent)
            return inverse((GuildBanEvent) event);

        else if (event instanceof GuildUnbanEvent)
            return inverse((GuildUnbanEvent) event);

        else if (event instanceof GuildMemberRoleAddEvent)
            return inverse((GuildMemberRoleAddEvent) event);

        else if (event instanceof GuildMemberRoleRemoveEvent)
            return inverse((GuildMemberRoleRemoveEvent) event);

        else if (event instanceof GuildMemberUpdateNicknameEvent)
            return inverse((GuildMemberUpdateNicknameEvent) event);

        else if (event instanceof GuildUpdateVanityCodeEvent)
            return inverse((GuildUpdateVanityCodeEvent) event);

        else if (event instanceof GuildUpdateMFALevelEvent)
            return inverse((GuildUpdateMFALevelEvent) event);

        else if (event instanceof GuildUpdateSystemChannelEvent)
            return inverse((GuildUpdateSystemChannelEvent) event);

        else if (event instanceof GuildUpdateRegionEvent)
            return inverse((GuildUpdateRegionEvent) event);

        else if (event instanceof GuildUpdateNameEvent)
            return inverse((GuildUpdateNameEvent) event);

        else if (event instanceof GuildUpdateDescriptionEvent )
            return inverse((GuildUpdateDescriptionEvent) event);

        else if (event instanceof GuildUpdateExplicitContentLevelEvent)
            return inverse((GuildUpdateExplicitContentLevelEvent) event);

        else if (event instanceof GuildUpdateNotificationLevelEvent)
            return inverse((GuildUpdateNotificationLevelEvent) event);

        else if (event instanceof GuildUpdateVerificationLevelEvent)
            return inverse((GuildUpdateVerificationLevelEvent) event);

        else if (event instanceof GuildUpdateAfkTimeoutEvent)
            return inverse((GuildUpdateAfkTimeoutEvent) event);

        else if (event instanceof GuildUpdateAfkChannelEvent)
            return inverse((GuildUpdateAfkChannelEvent) event);

        else if (event instanceof GuildMemberJoinEvent)
            return inverse((GuildMemberJoinEvent) event);

        else if (event instanceof GuildVoiceGuildDeafenEvent)
            return inverse((GuildVoiceGuildDeafenEvent) event);

        else if (event instanceof GuildVoiceJoinEvent)
            return inverse((GuildVoiceJoinEvent) event);

        else if (event instanceof GuildVoiceMoveEvent)
            return inverse((GuildVoiceMoveEvent) event);

        else if (event instanceof GuildVoiceGuildMuteEvent)
            return inverse((GuildVoiceGuildMuteEvent) event);

        else if (event instanceof PermissionOverrideCreateEvent)
            return inverse((PermissionOverrideCreateEvent) event);

        else if (event instanceof PermissionOverrideDeleteEvent)
            return inverse((PermissionOverrideDeleteEvent) event);

        else if (event instanceof PermissionOverrideUpdateEvent)
            return inverse((PermissionOverrideUpdateEvent) event);

        else if (event instanceof EmoteAddedEvent)
            return inverse((EmoteAddedEvent) event);

        else if (event instanceof EmoteUpdateNameEvent)
            return inverse((EmoteUpdateNameEvent) event);

        else if (event instanceof EmoteUpdateRolesEvent)
            return inverse((EmoteUpdateRolesEvent) event);

        else if (event instanceof RoleCreateEvent)
            return inverse((RoleCreateEvent) event);

        else if (event instanceof RoleDeleteEvent)
            return inverse((RoleDeleteEvent) event);

        else if (event instanceof RoleUpdateMentionableEvent)
            return inverse((RoleUpdateMentionableEvent) event);

        else if (event instanceof RoleUpdatePositionEvent)
            return inverse((RoleUpdatePositionEvent) event);

        else if (event instanceof RoleUpdatePermissionsEvent)
            return inverse((RoleUpdatePermissionsEvent) event);

        else if (event instanceof RoleUpdateNameEvent)
            return inverse((RoleUpdateNameEvent) event);

        else if (event instanceof RoleUpdateHoistedEvent)
            return inverse((RoleUpdateHoistedEvent) event);

        else if (event instanceof RoleUpdateColorEvent)
            return inverse((RoleUpdateColorEvent) event);

        else if (event instanceof PrivateChannelCreateEvent)
            return inverse((PrivateChannelCreateEvent) event);

        else if (event instanceof VoiceChannelDeleteEvent)
            return inverse((VoiceChannelDeleteEvent) event);

        else if (event instanceof VoiceChannelCreateEvent)
            return inverse((VoiceChannelCreateEvent) event);

        else if (event instanceof VoiceChannelUpdateNameEvent)
            return inverse((VoiceChannelUpdateNameEvent) event);

        else if (event instanceof VoiceChannelUpdateParentEvent)
            return inverse((VoiceChannelUpdateParentEvent) event);

        else if (event instanceof VoiceChannelUpdatePositionEvent)
            return inverse((VoiceChannelUpdatePositionEvent) event);

        else if (event instanceof VoiceChannelUpdateBitrateEvent)
            return inverse((VoiceChannelUpdateBitrateEvent) event);

        else if (event instanceof VoiceChannelUpdateUserLimitEvent)
            return inverse((VoiceChannelUpdateUserLimitEvent) event);

        else if (event instanceof TextChannelCreateEvent)
            return inverse((TextChannelCreateEvent) event);

        else if (event instanceof TextChannelUpdateTopicEvent)
            return inverse((TextChannelUpdateTopicEvent) event);

        else if (event instanceof TextChannelUpdateNameEvent)
            return inverse((TextChannelUpdateNameEvent) event);

        else if (event instanceof TextChannelUpdateSlowmodeEvent)
            return inverse((TextChannelUpdateSlowmodeEvent) event);

        else if (event instanceof TextChannelUpdatePositionEvent)
            return inverse((TextChannelUpdatePositionEvent) event);

        else if (event instanceof TextChannelUpdateNSFWEvent)
            return inverse((TextChannelUpdateNSFWEvent) event);

        else if (event instanceof TextChannelUpdateParentEvent)
            return inverse((TextChannelUpdateParentEvent) event);

        else if (event instanceof MessageReactionAddEvent)
            return inverse((MessageReactionAddEvent) event);

        else if (event instanceof MessageReceivedEvent)
            return inverse((MessageReceivedEvent) event);

        else if (event instanceof StoreChannelUpdatePositionEvent)
            return inverse((StoreChannelUpdatePositionEvent) event);

        else if (event instanceof StoreChannelUpdateNameEvent)
            return inverse((StoreChannelUpdateNameEvent) event);

        else if (event instanceof StoreChannelCreateEvent)
            return inverse((StoreChannelCreateEvent) event);

        else
            throw new InversionException("No possible inversion for event: " + event);
    }

    private static ChannelManager inverse(CategoryUpdatePositionEvent event)
    {
        ChannelManager manager = event.getCategory().getManager();
        int oldPosition = event.getOldPosition();

        return manager.setPosition(oldPosition);
    }

    private static ChannelManager inverse(CategoryUpdateNameEvent event)
    {
        ChannelManager manager = event.getCategory().getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static AuditableRestAction<Void> inverse(CategoryCreateEvent event)
    {
        return event.getCategory().delete();
    }

    private static AuditableRestAction<Void> inverse(GuildInviteCreateEvent event)
    {
        return event.getInvite().delete();
    }

    private static RestAction<Void> inverse(GuildJoinEvent event)
    {
        return event.getGuild().leave();
    }

    private static RestAction<Void> inverse(GuildBanEvent event)
    {
        Guild guild = event.getGuild();
        User user = event.getUser();

        return guild.unban(user);
    }

    private static AuditableRestAction<Void> inverse(GuildUnbanEvent event)
    {
        Guild guild = event.getGuild();
        User user = event.getUser();

        return guild.ban(user, 0);
    }

    private static AuditableRestAction<Void> inverse(GuildMemberRoleAddEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        List<Role> addedRoles = event.getRoles();

        return guild.modifyMemberRoles(member, null, addedRoles);
    }

    private static AuditableRestAction<Void> inverse(GuildMemberRoleRemoveEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        List<Role> rolesRemoved = event.getRoles();

        return guild.modifyMemberRoles(member, rolesRemoved, null);
    }

    private static AuditableRestAction<Void> inverse(GuildMemberUpdateNicknameEvent event)
    {
        Member member = event.getMember();
        String oldNick = event.getOldNickname();

        return member.modifyNickname(oldNick);
    }

    private static GuildManager inverse(GuildUpdateVanityCodeEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        String oldCode = event.getOldVanityCode();

        return manager.setVanityCode(oldCode);
    }

    private static GuildManager inverse(GuildUpdateMFALevelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Guild.MFALevel oldMFA = event.getOldMFALevel();

        return manager.setRequiredMFALevel(oldMFA);
    }

    private static GuildManager inverse(GuildUpdateSystemChannelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        TextChannel oldChannel = event.getOldSystemChannel();

        return manager.setSystemChannel(oldChannel);
    }

    private static GuildManager inverse(GuildUpdateRegionEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Region oldRegion = event.getOldRegion();

        return manager.setRegion(oldRegion);
    }

    private static GuildManager inverse(GuildUpdateNameEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static GuildManager inverse(GuildUpdateDescriptionEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        String oldDescription = event.getOldDescription();

        return manager.setDescription(oldDescription);
    }

    private static GuildManager inverse(GuildUpdateExplicitContentLevelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Guild.ExplicitContentLevel oldLevel = event.getOldValue();

        return manager.setExplicitContentLevel(oldLevel);
    }

    private static GuildManager inverse(GuildUpdateNotificationLevelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Guild.NotificationLevel oldLevel = event.getOldNotificationLevel();

        return manager.setDefaultNotificationLevel(oldLevel);
    }

    private static GuildManager inverse(GuildUpdateVerificationLevelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Guild.VerificationLevel oldLevel = event.getOldVerificationLevel();

        return manager.setVerificationLevel(oldLevel);
    }

    private static GuildManager inverse(GuildUpdateAfkTimeoutEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        Guild.Timeout oldTimeout = event.getOldAfkTimeout();

        return manager.setAfkTimeout(oldTimeout);
    }

    private static GuildManager inverse(GuildUpdateAfkChannelEvent event)
    {
        Guild guild = event.getGuild();
        GuildManager manager = guild.getManager();
        VoiceChannel oldChannel = event.getOldAfkChannel();

        return manager.setAfkChannel(oldChannel);
    }

    private static AuditableRestAction<Void> inverse(GuildMemberJoinEvent event)
    {
        return event.getMember().kick();
    }

    private static RestAction<Void> inverse(GuildVoiceGuildDeafenEvent event)
    {
        return event.getGuild().leave();
    }

    private static RestAction<Void> inverse(GuildVoiceJoinEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        return guild.kickVoiceMember(member);
    }

    private static RestAction<Void> inverse(GuildVoiceMoveEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        VoiceChannel previous = event.getOldValue();

        return guild.moveVoiceMember(member, previous);
    }

    private static AuditableRestAction<Void> inverse(GuildVoiceGuildMuteEvent event)
    {
        Member member = event.getMember();
        boolean action = event.isGuildMuted();

        return member.mute(!action);
    }

    private static AuditableRestAction<Void> inverse(PermissionOverrideCreateEvent event)
    {
        return event.getPermissionOverride().delete();
    }

    private static PermissionOverrideAction inverse(PermissionOverrideDeleteEvent event)
    {
        PermissionOverride deleted = event.getPermissionOverride();
        IPermissionHolder holder = event.getPermissionHolder();
        EnumSet<Permission> allowed = deleted.getAllowed();
        EnumSet<Permission> denied = deleted.getDenied();

        if (holder == null)
            throw new InversionException("No permission holder in cache to use to copy deleted override");

        return event.getChannel()
                    .createPermissionOverride(holder)
                    .setPermissions(allowed, denied);
    }

    private static PermissionOverrideAction inverse(PermissionOverrideUpdateEvent event)
    {
        PermissionOverride updated = event.getPermissionOverride();
        EnumSet<Permission> allow = event.getOldAllow();
        EnumSet<Permission> deny = event.getOldDeny();

        return updated.getManager()
                      .setAllow(allow).setDeny(deny);
    }

    private static AuditableRestAction<Void> inverse(EmoteAddedEvent event)
    {
        return event.getEmote().delete();
    }

    private static EmoteManager inverse(EmoteUpdateNameEvent event)
    {
        Emote emote = event.getEmote();
        EmoteManager manager = emote.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static EmoteManager inverse(EmoteUpdateRolesEvent event)
    {
        Emote emote = event.getEmote();
        EmoteManager manager = emote.getManager();
        HashSet<Role> oldRoles = new HashSet<>(event.getOldRoles());

        return manager.setRoles(oldRoles);
    }

    private static AuditableRestAction<Void> inverse(RoleCreateEvent event)
    {
        return event.getRole().delete();
    }

    private static RoleAction inverse(RoleDeleteEvent event)
    {
        return event.getRole().createCopy();
    }

    private static RoleManager inverse(RoleUpdateMentionableEvent event)
    {
        Role role = event.getRole();
        RoleManager manager = role.getManager();
        Boolean oldState = event.getOldValue();

        return manager.setMentionable(oldState);
    }

    private static OrderAction<Role, RoleOrderAction> inverse(RoleUpdatePositionEvent event)
    {
        Role role = event.getRole();
        int oldPos = event.getOldPosition();

        return event.getGuild()
                    .modifyRolePositions()
                    .selectPosition(role)
                    .moveTo(oldPos);
    }

    private static RoleManager inverse(RoleUpdatePermissionsEvent event)
    {
        Role role = event.getRole();
        RoleManager manager = role.getManager();
        EnumSet<Permission> oldPerms = event.getOldPermissions();

        return manager.setPermissions(oldPerms);
    }

    private static RoleManager inverse(RoleUpdateNameEvent event)
    {
        Role role = event.getRole();
        RoleManager manager = role.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static RoleManager inverse(RoleUpdateHoistedEvent event)
    {
        Role role = event.getRole();
        RoleManager manager = role.getManager();
        Boolean oldState = event.getOldValue();

        return manager.setHoisted(oldState);
    }

    private static RoleManager inverse(RoleUpdateColorEvent event)
    {
        Role role = event.getRole();
        RoleManager manager = role.getManager();
        Color oldColor = event.getOldColor();

        return manager.setColor(oldColor);
    }

    private static RestAction<Void> inverse(PrivateChannelCreateEvent event)
    {
        return event.getChannel().close();
    }

    private static ChannelAction<VoiceChannel> inverse(VoiceChannelDeleteEvent event)
    {
        Guild guild = event.getGuild();
        VoiceChannel deleted = event.getChannel();

        return guild.createVoiceChannel(deleted.getName())
                    .setUserlimit(deleted.getUserLimit())
                    .setBitrate(deleted.getBitrate())
                    .setPosition(deleted.getPosition())
                    .setParent(deleted.getParent());
    }

    private static AuditableRestAction<Void> inverse(VoiceChannelCreateEvent event)
    {
        return event.getChannel().delete();
    }

    private static ChannelManager inverse(VoiceChannelUpdateNameEvent event)
    {
        VoiceChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static ChannelManager inverse(VoiceChannelUpdateParentEvent event)
    {
        VoiceChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        Category oldParent = event.getOldParent();

        return manager.setParent(oldParent);
    }

    private static ChannelManager inverse(VoiceChannelUpdatePositionEvent event)
    {
        VoiceChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldPos = event.getOldPosition();

        return manager.setPosition(oldPos);
    }

    private static ChannelManager inverse(VoiceChannelUpdateBitrateEvent event)
    {
        VoiceChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldBitrate = event.getOldBitrate();

        return manager.setBitrate(oldBitrate);
    }

    private static ChannelManager inverse(VoiceChannelUpdateUserLimitEvent event)
    {
        VoiceChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldLimit = event.getOldUserLimit();

        return manager.setUserLimit(oldLimit);
    }

    private static AuditableRestAction<Void> inverse(TextChannelCreateEvent event)
    {
        return event.getChannel().delete();
    }

    private static ChannelManager inverse(TextChannelUpdateTopicEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        String oldTopic = event.getOldTopic();

        return manager.setTopic(oldTopic);
    }

    private static ChannelManager inverse(TextChannelUpdateNameEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static ChannelManager inverse(TextChannelUpdateSlowmodeEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldState = event.getOldSlowmode();

        return manager.setSlowmode(oldState);
    }

    private static ChannelManager inverse(TextChannelUpdatePositionEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldPos = event.getOldPosition();

        return manager.setPosition(oldPos);
    }

    private static ChannelManager inverse(TextChannelUpdateNSFWEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        boolean oldState = event.getOldNSFW();

        return manager.setNSFW(oldState);
    }

    private static ChannelManager inverse(TextChannelUpdateParentEvent event)
    {
        TextChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        Category oldParent = event.getOldParent();

        return manager.setParent(oldParent);
    }

    private static RestAction<Void> inverse(MessageReactionAddEvent event)
    {
        MessageReaction reaction = event.getReaction();
        User user = event.getUser();

        if (user == null)
            throw new InversionException("No cached user to remove reaction from");

        return reaction.removeReaction(user);
    }

    private static AuditableRestAction<Void> inverse(MessageReceivedEvent event)
    {
        return event.getMessage().delete();
    }

    private static ChannelManager inverse(StoreChannelUpdatePositionEvent event)
    {
        StoreChannel updated = event.getChannel();
        ChannelManager manager = updated.getManager();
        int oldPos = event.getOldPosition();

        return manager.setPosition(oldPos);
    }

    private static ChannelManager inverse(StoreChannelUpdateNameEvent event)
    {
        StoreChannel update = event.getChannel();
        ChannelManager manager = update.getManager();
        String oldName = event.getOldName();

        return manager.setName(oldName);
    }

    private static AuditableRestAction<Void> inverse(StoreChannelCreateEvent event)
    {
        return event.getChannel().delete();
    }

    private static class InversionException extends RuntimeException
    {
        public InversionException(String msg)
        {
            super(msg);
        }
    }
}

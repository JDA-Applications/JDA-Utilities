/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

/**
 * Sends generic information about the bot
 *
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
    name = "About",
    description = "Gets information about the bot."
)
@Author(" John Grosh (jagrosh)")
public class AboutCommand extends Command
{

    private final Color color;
    private final String description;
    private final Command[] commands;
    private Permission[] perms;
    private String inviteLink;

    /**
     * Initialize an About command to send information about your bot!
     *
     * @param color The Embed Color
     * @param description The bot's Description
     * @param commands All the commands to show
     * @param invPerms Permissions to generate an Invite Link
     */
    public AboutCommand(Color color, String description, Command[] commands,
                        Permission... invPerms)
    {
        //Super variables
        this(color, description, commands);

        //About Command variables
        this.perms = invPerms;
        this.inviteLink = null;
    }

    /**
     * Initialize an About command to send information about your bot!
     *
     * @param color The Embed Color
     * @param description The bot's Description
     * @param commands All the commands to show
     * @param inviteLink The invite link for the bot
     */
    public AboutCommand(Color color, String description, Command[] commands,
                        String inviteLink)
    {
        //Super variables
        this(color,description, commands);

        //About Command variables
        this.inviteLink = inviteLink;
        this.perms = null;
    }

    /**
     * The default constructor, this is used to clean the clutter from other constructors
     *
     * @param color The Embed Color
     * @param description The bot's Description
     * @param commands All the commands to show
     */
    private AboutCommand(Color color, String description, Command[] commands)
    {
        //Super variables
        this.name = "About";
        this.aliases = new String[] {"whoareyou"};
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

        //About Command variables
        this.color = color;
        this.description = description;
        this.commands = commands;
    }


    @Override
    protected void execute(CommandEvent event)
    {
        //Get info about the bot
        event.getJDA().asBot().getApplicationInfo().queue(info -> {
            if (inviteLink == null) //Set the invite link if not set yet
                inviteLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            send(event); // Send the About embed
        }, failure -> {
            //Log the failure
            Logger log = LoggerFactory.getLogger("OAuth2");
            log.error("Could not generate invite link ", failure);
            inviteLink = "";
            send(event); //Send the About embed anyway
        });
    }

    private void send(CommandEvent event)
    {
        //Save these here so code is less messy
        JDA jda = event.getJDA();
        CommandClient cmdClient = event.getClient();
        User self = event.getSelfUser();
        //This gets the Bot Owner as a user Mention
        // Make sure to set this in your Command Client with .setOwnerId()
        String ownerMention = "<@" + cmdClient.getOwnerId() + ">";
        String serverInv = cmdClient.getServerInvite();

        StringBuilder sb = new StringBuilder();

        //Setup the basic parts of the EmbedBuilder
        EmbedBuilder builder = new EmbedBuilder()
            .setColor(color)
            .setAuthor(self.getName(), null, self.getAvatarUrl())
            .setTitle("About " + self.getName() + "!")
            .setFooter("Last restart", self.getAvatarUrl())
            .setTimestamp(cmdClient.getStartTime());

        //Use a StringBuilder cuz nice
        sb.append("Hello! I am **").append(self.getName()).append("**\n")
          .append("I was made by **").append(ownerMention)
          .append("** using " + JDAUtilitiesInfo.AUTHOR + "'s ")
          .append("[`JDA Utilities ").append(JDAUtilitiesInfo.VERSION).append("`]")
          .append("(" + JDAUtilitiesInfo.GITHUB + ") ")
          .append("and the [`JDA ").append(JDAInfo.VERSION).append(" library`]")
          .append("(https://github.com/DV8FromTheWorld/JDA)\n")
          .append(this.description).append("\n\nType ``")
          .append(cmdClient.getTextualPrefix()).append(cmdClient.getHelpWord())
          .append("`` for help using my commands!\n");

        //Append the server invites if they are available
        if (serverInv != null && !serverInv.isEmpty())
        {
            sb.append("Join [`my server here`](").append(serverInv).append(") and \n");
        }
        if (inviteLink != null && !inviteLink.isEmpty())
        {
            sb.append("Please [`invite me to your server!`](")
              .append(inviteLink).append(") ");
        }

        sb.append("\n\nMy Features include: ```css"); //Use CSS style here for coloring
        for (Command cmd: commands)
        {
            if (cmd.isOwnerCommand()) continue;
            //You may need to use a Menu Paginator if there are too many Commands
            //  for 1 MessageEmbed
            sb.append(cmd.getName()).append("\n");
        }
        sb.append(" ```");

        builder.setDescription(sb);

        //Add info about servers & shards
        JDA.ShardInfo shardInfo = jda.getShardInfo();
        List<Guild> guilds = jda.getGuilds();

        if (shardInfo == null)
        { //If not using Shards
            //Guild Number
            builder.addField("Stats", guilds.size() + " servers\n1 shard", true);
            //User Count
            builder.addField("Users", jda.getUsers().size() + " unique\n" +
                guilds.stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            //Channel Count
            builder.addField("Channels", jda.getTextChannels().size() + " Text\n" +
                                 jda.getVoiceChannels().size() + " Voice", true);
        } else { //If using Shards
            //Guild & Shard count
            builder.addField("Stats", cmdClient.getTotalGuilds() + " Servers\nShard " +
                (shardInfo.getShardId() + 1) + "/" + shardInfo.getShardTotal(), true);
            //Shard info (User & Server count)
            builder.addField("This shard", jda.getUsers().size() + " Users\n" +
                guilds.size() + " Servers", true);
            //Channel Info
            builder.addField("", jda.getTextChannels().size() +
                " Text Channels\n" + jda.getVoiceChannels().size() +
                " Voice Channels", true);
        }

        event.reply(builder.build());
    }

}

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
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.doc.standard.Error;
import com.jagrosh.jdautilities.doc.standard.RequiredPermissions;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A Command that sends a list of all Guilds which the bot is a member of.
 *
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
    name = "Guildlist",
    description = "Gets a paginated list of the guilds the bot is on.",
    requirements = {
        "The bot has all necessary permissions.",
        "The user is the bot's owner."
    }
)
@Error(
    value = "If arguments are provided, but they are not an integer.",
    response = "[PageNumber] is not a valid integer!"
)
@RequiredPermissions({Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION})
@Author("John Grosh (jagrosh)")
public class GuildlistCommand extends Command
{

    private final EventWaiter waiter;

    public GuildlistCommand(EventWaiter waiter)
    {
        this.name = "guildlist";
        this.aliases = new String[]{"listguilds", "guilds"};
        this.help = "shows the list of guilds the bot is on";
        this.arguments = "[pagenum]";
        this.guildOnly = false;
        this.ownerCommand = true;
        this.botPermissions = new Permission[]{
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION
        };

        this.waiter = waiter;
    }

    /** @return a new Paginator */
    private Paginator.Builder getBuilder(String text)
    {
        Checks.notNull(text, "text");
        return new Paginator.Builder().setItemsPerPage(10)
                            .showPageNumbers(true).waitOnSinglePage(false)
                            .useNumberedItems(true).setEventWaiter(this.waiter)
                            .setTimeout(1, TimeUnit.MINUTES)
                            .setText(text).setFinalAction(m -> {
                try {
                    m.clearReactions().queueAfter(250, TimeUnit.MILLISECONDS);
                } catch (PermissionException ex) {
                    m.delete().queue();
                }
            });
    }

    /**
     * @param guild The guild to count
     * @return the number of members which are not bots
     */
    private static int getHumanCount(Guild guild)
    {
        return guild.getMembers().size() - guild.getMembers().stream()
                                                .filter(m-> m.getUser().isBot())
                                                .toArray().length;
    }

    private static double percentHuman(Guild guild)
    {
        return Math.round((getHumanCount(guild) * 1.0)/guild.getMembers().size() * 100);
    }

    @Override
    protected void execute(CommandEvent event)
    {

        List<Guild> guilds = event.getJDA().getGuilds();
        //Sort by number of non-bots
        guilds.sort(Comparator.comparingInt(GuildlistCommand::getHumanCount));

        String text = "All guilds housing " + event.getSelfUser().getName()
            + (event.getJDA().getShardInfo()==null ? ":" : "(Shard ID "+event.getJDA().getShardInfo().getShardId()+"):")
            + "\n(Total Guilds: " + guilds.size() + ")"
            + "\n``Name (ID) ~ member count (percentage non-bots)``";

        Paginator.Builder builder = getBuilder(text);

        //Add all the guilds to the paginator
        guilds.stream().map( g -> //Convert the list of guilds to a stream of Strings
            g.getName() + " (" + "ID: " + g.getId() + ") "+ " ~ " + g.getMembers().size()
                + " (" + percentHuman(g) + "%)"
        ).forEach(builder::addItems);

        int page = 1;
        if(!event.getArgs().isEmpty())
        {
            try
            {
                page = Integer.parseInt(event.getArgs());
            }
            catch(NumberFormatException e)
            {
                event.reply(event.getClient().getError() + " `"
                                +event.getArgs() + "` is not a valid integer!");
                return;
            }
        }

        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.black)
               .setUsers(event.getAuthor())
               .build()
               .paginate(event.getChannel(), page);
    }

}



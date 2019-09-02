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
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class RoleinfoCommand extends Command
{
    private final static String LINESTART = "\u25AB"; // ▫
    private final static String ROLE_EMOJI = "\uD83C\uDFAD"; // 🎭
    
    public RoleinfoCommand()
    {
        this.name = "roleinfo";
        this.aliases = new String[]{"rinfo","rankinfo"};
        this.help = "shows info about a role";
        this.arguments = "<role>";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        Role role;
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please provide the name of a role!");
            return;
        }
        else
        {
            List<Role> found = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if(found.isEmpty())
            {
                event.replyError("I couldn't find the role you were looking for!");
                return;
            }
            else if(found.size()>1)
            {
                event.replyWarning(listOfRoles(found, event.getArgs()));
                return;
            }
            else
            {
                role = found.get(0);
            }
        }
        
        String title = (ROLE_EMOJI + " Information about **" + role.getName() + "**:")
                .replace("@everyone", "@\u0435veryone") // cyrillic e
                .replace("@here", "@h\u0435re") // cyrillic e
                .replace("discord.gg/", "dis\u0441ord.gg/"); // cyrillic c;;
        List<Member> list = role.isPublicRole() ? event.getGuild().getMembers() : event.getGuild().getMembersWithRoles(role);
        Color color = role.getColor();
        StringBuilder desr = new StringBuilder(LINESTART + "ID: **" + role.getId() + "**\n"
                + LINESTART + "Creation: **" + role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME)+"**\n"
                + LINESTART + "Position: **" + role.getPosition()+"**\n"
                + LINESTART + "Color: **#" + (color==null ? "000000" : Integer.toHexString(color.getRGB()).toUpperCase().substring(2)) + "**\n"
                + LINESTART + "Mentionable: **" + role.isMentionable() + "**\n"
                + LINESTART + "Hoisted: **" + role.isHoisted() + "**\n"
                + LINESTART + "Managed: **" + role.isManaged() + "**\n"
                + LINESTART + "Permissions: ");
        if(role.getPermissions().isEmpty())
            desr.append("None");
        else
            desr.append(role.getPermissions().stream().map(p -> "`, `"+p.getName()).reduce("", String::concat).substring(3)).append("`");
        desr.append("\n").append(LINESTART).append("Members: **").append(list.size()).append("**\n");
        if(list.size() * 24 <= 2048-desr.length())
            list.forEach(m -> desr.append("<@").append(m.getUser().getId()).append("> "));
        
        event.reply(new MessageBuilder()
                .append(title)
                .setEmbed(new EmbedBuilder()
                        .setDescription(desr.toString().trim())
                        .setColor(role.getColor()).build())
                .build());
    }
    
    private static String listOfRoles(List<Role> list, String query)
    {
        String out = String.format("**Multiple roles found matching \"%s\":**", query);
        for(int i = 0; i < 6 && i < list.size(); i++)
            out += "\n - " + list.get(i).getName() + " (ID:" + list.get(i).getId() + ")";
        if(list.size() > 6)
            out += "\n**And " + (list.size() - 6) + " more...**";
        return out;
    }
}

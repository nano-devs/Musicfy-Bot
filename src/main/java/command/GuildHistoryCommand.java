package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildHistoryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class GuildHistoryCommand extends Command
{
    private final NanoClient client;

    public GuildHistoryCommand(NanoClient client)
    {
        this.client = client;

//        this.name = "history_guild";
        this.name = "histg";
//        this.aliases = new String[]{"histg", "hist g", "histu guild"};
        this.guildOnly = true;
        this.help = "Get all guild history.\n";
        this.cooldown = 2;
        this.category = new Category("History");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        GuildHistoryModel db = new GuildHistoryModel();
        String message = db.GetGuildHistory(event.getGuild().getIdLong(), event.getJDA());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        embed.setTitle(":calendar_spiral: Your " + event.getGuild().getName() + " guild history");
        embed.setThumbnail(event.getGuild().getIconUrl());
        embed.setDescription(message);

        event.replyInDm(embed.build());
    }
}

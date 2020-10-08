package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildHistoryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;

public class GuildHistoryCommand extends Command
{
    private final JDA client;

    public GuildHistoryCommand(JDA client)
    {
        this.client = client;

        this.name = "history guild";
        this.aliases = new String[]{"histg", "hist g", "histu guild"};
        this.guildOnly = true;
        this.help = "Get all user history";
        this.cooldown = 2;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        GuildHistoryModel db = new GuildHistoryModel();
        String message = db.GetGuildHistory(event.getGuild().getIdLong(), this.client);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.MAGENTA);
        embed.setTitle("Your " + event.getGuild().getName() + " guild history");
        embed.setDescription(message);

        event.replyInDm(embed.build());
    }
}

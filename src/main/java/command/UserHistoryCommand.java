package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserHistoryModel;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class UserHistoryCommand extends Command
{
    public UserHistoryCommand()
    {
//        this.name = "history_user";
        this.name = "histu";
//        this.aliases = new String[]{"histu", "hist u", "hist user"};
        this.guildOnly = true;
        this.help = "Get all user history, max 2048 characters";
        this.cooldown = 2;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        UserHistoryModel db = new UserHistoryModel();
        String message = db.GetUserHistory(event.getAuthor().getIdLong());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.MAGENTA);
        embed.setTitle(":calendar_spiral: Your history");
        embed.setThumbnail(event.getAuthor().getAvatarUrl());
        embed.setDescription(message);

        event.replyInDm(embed.build());
    }
}

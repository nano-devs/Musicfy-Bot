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
        this.name = "history user";
        this.aliases = new String[]{"histu", "hist u", "hist user"};
        this.guildOnly = true;
        this.help = "Get all user history";
        this.cooldown = 2;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        UserHistoryModel db = new UserHistoryModel();
        String message = db.GetUserHistory(event.getAuthor().getIdLong());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.MAGENTA);
        embed.setTitle("Your history ");
        embed.setThumbnail(event.getAuthor().getAvatarUrl());
        embed.setDescription("\uFE0F" + message);

        event.replyInDm(embed.build());
    }
}

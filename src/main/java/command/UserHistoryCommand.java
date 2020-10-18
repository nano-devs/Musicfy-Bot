package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserHistoryModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.awt.*;

public class UserHistoryCommand extends Command
{
    public UserHistoryCommand()
    {
//        this.name = "history_user";
        this.name = "histu";
//        this.aliases = new String[]{"histu", "hist u", "hist user"};
        this.guildOnly = true;
        this.help = "Get all user history.\n";
        this.cooldown = 2;
        this.category = new Category("History");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        UserHistoryModel db = new UserHistoryModel();
        String message = db.GetUserHistory(event.getAuthor().getIdLong());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        embed.setTitle(":calendar_spiral: Your history");
        embed.setThumbnail(event.getAuthor().getAvatarUrl());
        embed.setDescription(message);

        event.replyInDm(embed.build());
    }
}

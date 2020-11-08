package command.history;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserHistoryModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

public class UserHistoryCommand extends Command
{
    public UserHistoryCommand()
    {
//        this.name = "history_user";
        this.name = "histu";
//        this.aliases = new String[]{"histu", "hist u", "hist user"};
        this.guildOnly = true;
        this.help = "Get all user history.";
        this.cooldown = 2;
        this.category = new Category("History");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        UserHistoryModel db = new UserHistoryModel();
        String message = db.getUserHistory(event.getAuthor().getIdLong());

        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        embed.setTitle(":calendar_spiral: Your history");
        embed.setThumbnail(event.getAuthor().getAvatarUrl());
        embed.setDescription(message);

        event.replyInDm(embed.build());

        event.getMessage().addReaction("\uD83C\uDFA7").queue();
    }
}

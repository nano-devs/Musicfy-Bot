package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.ClassicUser;
import database.UserModel;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class VoteCommand extends Command {

    public VoteCommand(String prefix) {
        this.name="vote";
        this.category = new Category("Daily Event");
        this.help = "Vote daily and claim recommendation quota. `Bonus at weekend`. use "
                + prefix + "quota to claim";
        this.cooldown = 2;
        this.aliases = new String[] {"daily"};
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        UserModel userModel = new UserModel();

        ClassicUser classicUser;
        try {
            classicUser = userModel.read(event.getAuthor().getIdLong());
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return;
        }

        if (classicUser == null) {
            CompletableFuture.runAsync(() -> {
                try {
                    userModel.create(event.getAuthor().getIdLong(), 0, 1);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            });
        }

        String voteUrl = "";
        String message = "You have " + classicUser.getRecommendationQuota() + " quotas in stock. Vote and use **" +
                event.getClient().getPrefix() + "claim** to claim quota\n" + voteUrl;
        event.reply(message);
    }
}

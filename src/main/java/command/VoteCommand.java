package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.ClassicUser;
import database.UserModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class VoteCommand extends Command {

    NanoClient nanoClient;

    public VoteCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name="vote";
        this.category = new Category("Daily Event");
        this.help = "Vote daily and claim rewards `More rewards at weekend` :love_letter:";
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
                    userModel.create(event.getAuthor().getIdLong(), 3, 1);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            });
            classicUser.setDailyQuota(1);
        }

        EmbedBuilder embedBuilder = this.nanoClient.getEmbeddedVoteLink(classicUser, event);
        event.reply(embedBuilder.build());
    }
}

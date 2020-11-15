package command.general;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import database.Entity.ClassicUser;
import database.UserModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;
import service.music.MusicService;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class VoteCommand extends Command {
    CommandClientImpl impl;
    NanoClient nanoClient;

    public VoteCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name="vote";
        this.category = new Category("General");
        this.help = "Vote daily and claim rewards `More rewards at weekend` :love_letter:";
        this.cooldown = 2;
        this.aliases = new String[] {"daily", "stocks", "stock"};
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
            classicUser = new ClassicUser(event.getAuthor().getIdLong(), 1, 3);
            CompletableFuture.runAsync(() -> {
                try {
                    userModel.create(event.getAuthor().getIdLong(), 3, 1);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            });
            classicUser.setDailyQuota(1);
        }

        CustomEmbedBuilder embedBuilder = MusicService.getEmbeddedVoteLink(classicUser, event);
        
        event.reply(embedBuilder.build());
    }
}

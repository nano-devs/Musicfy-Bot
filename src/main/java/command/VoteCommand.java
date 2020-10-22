package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.ClassicUser;
import database.UserModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class VoteCommand extends Command {

    public VoteCommand(String prefix) {
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

        String voteUrl = "";
        String message = "Use **" +event.getClient().getPrefix() +
                "claim** command to claim rewards\n" + voteUrl;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(event.getMember().getColor());
        embedBuilder.setTitle(":headphones: | Thank you for using " + event.getSelfUser().getName() + "!");
        embedBuilder.setAuthor("My Stocks", event.getAuthor().getEffectiveAvatarUrl(),
                event.getAuthor().getEffectiveAvatarUrl());
        embedBuilder.addField("Daily Quota", String.valueOf(classicUser.getDailyQuota()), true);
        embedBuilder.addField("Claimed Reward", String.valueOf(classicUser.getRecommendationQuota()), true);
        embedBuilder.addField("Usage", message, false);
        embedBuilder.setFooter("Have a nice dayy~");
        event.reply(embedBuilder.build());
    }
}

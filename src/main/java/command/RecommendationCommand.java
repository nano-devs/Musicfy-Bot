package command;

import YouTubeSearchApi.YoutubeClient;
import YouTubeSearchApi.entity.YoutubePlaylist;
import YouTubeSearchApi.exception.NoResultFoundException;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.ClassicUser;
import database.UserModel;
import service.music.*;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class RecommendationCommand extends Command {

    private NanoClient nanoClient;
    private YoutubeClient youtubeClient;

    public RecommendationCommand(NanoClient nanoClient, YoutubeClient youtubeClient) {
        this.nanoClient = nanoClient;
        this.youtubeClient = youtubeClient;

        this.name = "recommend";
        this.arguments = "<a number in range 1-24>";
        this.aliases = new String[] {"play_recommendation", "pr", "play_r", "rec"};
        this.category = new Category("Music");
        this.cooldown = 2;
        this.guildOnly = true;
        this.help = "Add song recommendation (based on currently playing song) to queue\n" +
                ":warning: Using this command twice or multiple times on the same song " +
                "might result the same recommendation.";
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply(":x: | You're not connected to any voice channel.");
            return;
        }

        // Ensure argument
        int requestNumber = -1;
        // if argument is empty
        if (event.getArgs().isEmpty()) {
            event.reply(":x: | Please provide a number `(1 - 24)`. Example `"
                    +event.getClient().getPrefix()+"recommend 5` to request 5 recommendations");
            return;
        }
        else {
            // Check if argument is a number
            try {
                requestNumber = Integer.parseInt(event.getArgs());
            } catch (Exception e) {
                event.reply(":x: | Please provide a number `(1 - 24)`. Example `"
                        +event.getClient().getPrefix()+"recommend 5` to request 5 recommendations");
                return;
            }

            // if a number, then check if number is in valid range.
            if (requestNumber <= 0 || requestNumber > 24) {
                event.reply(":x: | Please provide a number `(1 - 24)`. Example `"
                        +event.getClient().getPrefix()+"recommend 5` to request 5 recommendations");
                return;
            }
        }

        GuildMusicManager musicManager = this.nanoClient.getGuildAudioPlayer(event.getGuild());
        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        String sourceName = musicManager.player.getPlayingTrack().getSourceManager().getSourceName();
        if (musicManager.player.getPlayingTrack() == null && !sourceName.equals("youtube")) {
            event.reply(":x: | Play a `youtube` song first and try `m$recommend " + event.getArgs() + "` again :>");
            return;
        }

        UserModel userModel = new UserModel();
        ClassicUser classicUser;
        try {
             classicUser = userModel.read(event.getAuthor().getIdLong());
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return;
        }

        // if user is not registered. make new user record and recommend.
        if (classicUser == null) {
            recommend(event, musicManager, requestNumber);
            CompletableFuture.runAsync(() -> {
                try {
                    userModel.create(event.getAuthor().getIdLong(), 5, 0);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            });
            return;
        }

        // if user is registered. then Check user's daily quota (priority)
        if (classicUser.getDailyQuota() > 0) {
            // Recommend & Update quota
            recommend(event, musicManager, requestNumber);
            CompletableFuture.runAsync(() -> {
                try {
                    userModel.updateDailyQuota(classicUser.getId(),
                            classicUser.getDailyQuota() - 1);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            });
            return;
        }

        // if Daily Quota not available & Claimed quota is not available
        if (classicUser.getRecommendationQuota() < 1) {
            CustomEmbedBuilder embedBuilder = MusicService.getEmbeddedVoteLink(classicUser, event);
            event.reply(embedBuilder.build());
            return;
        }

        recommend(event, musicManager, requestNumber);
        // if quota is available
        CompletableFuture.runAsync(() -> {
            try {
                userModel.updateRecommendationQuota(classicUser.getId(),
                        classicUser.getRecommendationQuota() - 1);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });
    }

    private void recommend(CommandEvent event, GuildMusicManager musicManager, int loadLimit) {
        String currentPlayingTrackId = musicManager.player.getPlayingTrack().getIdentifier();
        try {
            YoutubePlaylist youtubePlaylist = youtubeClient.getRecommendation(currentPlayingTrackId);
            this.nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(),
                    youtubePlaylist.getUrl(), event.getMember(), loadLimit);
        } catch (IOException | NoResultFoundException e) {
            event.reply(":x: | " + e.getMessage());
        }
    }
}

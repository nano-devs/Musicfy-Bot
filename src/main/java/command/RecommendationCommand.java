package command;

import YouTubeSearchApi.YoutubeClient;
import YouTubeSearchApi.entity.YoutubePlaylist;
import YouTubeSearchApi.exception.NoResultFoundException;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.io.IOException;

public class RecommendationCommand extends Command {

    private NanoClient nanoClient;
    private YoutubeClient youtubeClient;

    public RecommendationCommand(NanoClient nanoClient, YoutubeClient youtubeClient) {
        this.nanoClient = nanoClient;
        this.youtubeClient = youtubeClient;

        this.name = "recommend";
        this.aliases = new String[] {"play_recommendation", "pr", "play_r"};
        this.category = new Category("Music");
        this.cooldown = 2;
        this.guildOnly = true;
        this.help = "Add recommendation (based on current playing song) to queue";
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply(":x: | You're not connected to any voice channel");
            return;
        }

        // Check user quota here
        // ...

        // if quota is available
        GuildMusicManager musicManager = this.nanoClient.getGuildAudioPlayer(event.getGuild());
        String currentPlayingTrackId = musicManager.player.getPlayingTrack().getIdentifier();
        try {
            YoutubePlaylist youtubePlaylist = youtubeClient.getRecommendation(currentPlayingTrackId);
            this.nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(),
                    youtubePlaylist.getUrl(), event.getMember());
        } catch (IOException e) {
            event.reply(":x: | " + e.getMessage());
        } catch (NoResultFoundException e) {
            event.reply(":x: | " + e.getMessage());
        }
    }
}

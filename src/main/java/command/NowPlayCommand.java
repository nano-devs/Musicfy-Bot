package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.Music.GuildMusicManager;

public class NowPlayCommand extends Command {

    NanoClient nanoClient;

    public NowPlayCommand(NanoClient nanoClient) {
        this.name = "np";
        this.help = "Get now playing song.";
        this.aliases = new String[]{"nowplay", "now_play", "now_playing", "nowplaying"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        if (musicManager.player.getPlayingTrack() == null) {
            event.replyError("Not playing anything");
            return;
        }
        musicManager.announceNowPlaying(event);
    }
}

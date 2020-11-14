package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

public class NowPlayCommand extends Command {

    NanoClient nanoClient;

    public NowPlayCommand(NanoClient nanoClient) {
        this.name = "np";
        this.help = "Get now playing song.";
        this.aliases = new String[]{"nowplay", "now_play", "now_playing", "nowplaying"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
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

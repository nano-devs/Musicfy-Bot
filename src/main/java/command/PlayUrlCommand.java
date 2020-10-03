package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class PlayUrlCommand extends Command {

    private NanoClient nanoClient;

    public PlayUrlCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "purl";
        this.help = "Stands for `play_url`, play song from a given url. Supported urls: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo.";
        this.aliases = new String[] {"url", "play_url", "playurl"};
        this.cooldown = 2;
        this.arguments = "<url>";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!nanoClient.getMusicService().joinUserVoiceChannel(event)) {
            return;
        }
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setVolume(15);
        nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(), event.getArgs(), event.getAuthor());
    }
}

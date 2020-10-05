package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.validator.routines.UrlValidator;
import service.music.GuildMusicManager;

public class PlayCommand extends Command {

    private NanoClient nanoClient;

    public PlayCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "play";
        this.help = "play song from a given url or keywords. Supported urls: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo.";
        this.aliases = new String[] {"p"};
        this.cooldown = 2;
        this.arguments = "<url/keywords>";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply("You're not connected to any voice channel");
            return;
        }

        // check if client is connected to any voice channel
        AudioManager guildAudioManager = event.getGuild().getAudioManager();
        VoiceChannel connectedChannel = guildAudioManager.getConnectedChannel();

        if (connectedChannel == null) {
            // if not connected to any voice channel, try to join user voice channel.
            nanoClient.getMusicService().joinUserVoiceChannel(event);
        }
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        String args = event.getArgs();
        UrlValidator urlValidator = new UrlValidator(schemes);

        if (urlValidator.isValid(args)) {
            nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(), args, event.getAuthor());
        }
        else {
            nanoClient.loadAndPlayKeywords(musicManager, event.getTextChannel(), args, event.getAuthor());
        }
    }
}

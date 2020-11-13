package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.validator.routines.UrlValidator;
import service.music.*;

public class PlayUrlCommand extends Command {

    private NanoClient nanoClient;

    public PlayUrlCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "play_url";
        this.help = "Stands for `purl`, play song from a given url.\nSupported urls: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo.";
        this.aliases = new String[] {"url", "purl", "playurl"};
        this.cooldown = 2;
        this.arguments = "<url>";
        this.guildOnly = true;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply(":x: | You're not connected to any voice channel");
            return;
        }

        String args = event.getArgs();
        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (args.isEmpty() || !urlValidator.isValid(args)) {
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":x: | Invalid Arguments", "Example usage: "
                    + event.getClient().getPrefix() + this.name + " " + this.arguments, true);
            event.reply(embedBuilder.build());
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        if (musicManager.isQueueFull()) {
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":x: | Queue is full", "Maximum queue length is 60.", true);
            event.reply(embedBuilder.build());
            return;
        }

        // check if client is connected to any voice channel
        AudioManager guildAudioManager = event.getGuild().getAudioManager();
        VoiceChannel connectedChannel = guildAudioManager.getConnectedChannel();

        if (connectedChannel == null) {
            // if not connected to any voice channel, try to join user voice channel.
            nanoClient.getMusicService().joinUserVoiceChannel(event);
        }

        nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(), args, event.getMember());
    }
}

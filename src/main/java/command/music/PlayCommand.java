package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.validator.routines.UrlValidator;
import service.music.*;

public class PlayCommand extends Command {

    private NanoClient nanoClient;

    public PlayCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "play";
        this.help = "play song from a given url or keywords.\nSupported urls: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo.";
        this.aliases = new String[] {"p"};
        this.cooldown = 2;
        this.arguments = "<url/keywords>";
        this.guildOnly = true;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        if (args.isEmpty()) {
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":x: | Invalid Arguments", "Example usage: "
                    + event.getClient().getPrefix() + this.name + " " + this.arguments, true);
            event.reply(embedBuilder.build());
            return;
        }

        if (!nanoClient.getMusicService().ensureVoiceState(event)) {
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.scheduler.textChannel = event.getTextChannel();
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

        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);

        if (urlValidator.isValid(args)) {
            nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(), args, event.getMember());
        }
        else {
            nanoClient.loadAndPlayKeywords(musicManager, event.getTextChannel(), args, event.getMember());
        }
    }
}

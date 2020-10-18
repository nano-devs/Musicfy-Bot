package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.validator.routines.UrlValidator;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.PremiumService;

public class PlayUrlCommand extends Command {

    private NanoClient nanoClient;

    public PlayUrlCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "purl";
        this.help = "Stands for `play_url`, play song from a given url.\nSupported urls: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo.\n";
        this.aliases = new String[] {"url", "play_url", "playurl"};
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
            event.reply("You're not connected to any voice channel");
            return;
        }

        String args = event.getArgs();
        if (args.isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(event.getMember().getColor());
            embedBuilder.addField(":x: | Invalid Arguments", "Example usage: "
                    + event.getClient().getPrefix() + this.name + " " + this.arguments, true);
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

        PremiumService.addHistory("", event.getArgs(), event);

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        nanoClient.loadAndPlayUrl(musicManager, event.getTextChannel(), args, event.getMember());
    }
}

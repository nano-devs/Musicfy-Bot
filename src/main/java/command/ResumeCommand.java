package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class ResumeCommand extends Command {

    NanoClient nanoClient;

    public ResumeCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "resume";
        this.guildOnly = true;
        this.cooldown = 1;
        this.help = "Resume paused song";
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel userVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (userVoiceChannel == null) {
            event.reply(":x: | You are not connected to any voice channel");
            return;
        }

        GuildMusicManager musicManager = this.nanoClient.getGuildAudioPlayer(event.getGuild());

        if (musicManager.player.getPlayingTrack() == null) {
            event.reply(":x: | Not currently playing anything.");
            return;
        }

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        musicManager.player.setPaused(false);
        event.getMessage().addReaction("\u25B6").queue(); // Play Button
    }
}

package command.music;

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
        if (!nanoClient.getMusicService().ensureVoiceState(event)) {
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
        musicManager.setPauseStatus(false);
        event.getMessage().addReaction("\u25B6").queue(); // Play Button
    }
}

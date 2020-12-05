package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class JoinCommand extends Command {

    NanoClient nanoClient;

    public JoinCommand(NanoClient nanoClient) {
        this.name = "join";
        this.help = "Join user's voice channel";
        this.aliases = new String[]{"summon"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.scheduler.textChannel = event.getTextChannel();

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        VoiceChannel connectedChannel = nanoClient.getMusicService().joinMemberVoiceChannel(event);

        if (connectedChannel == null) {
            return;
        }

        event.reply(":white_check_mark: | Connected to :loud_sound: `" + connectedChannel.getName() + "`");
    }
}

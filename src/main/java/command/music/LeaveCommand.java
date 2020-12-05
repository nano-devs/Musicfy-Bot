package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class LeaveCommand extends Command {
    NanoClient nanoClient;

    public LeaveCommand(NanoClient nanoClient) {
        this.name = "leave";
        this.help = "Stop playing music and leaves voice channel";
        this.aliases = new String[]{"stop"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();

        VoiceChannel selfVoiceChannel = event.getSelfMember().getVoiceState().getChannel();
        if (selfVoiceChannel == null) {
            return;
        }

        if (event.getMember().getVoiceState().getChannel() == null) {
            return;
        }

         if (!selfVoiceChannel.getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
            return;
        }

        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        if (!nanoClient.getMusicManagers().containsKey(guild.getIdLong())) {
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(guild);

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        musicManager.scheduler.setInLoopState(false);
        musicManager.scheduler.getQueue().clear();
        musicManager.player.destroy();
        nanoClient.getMusicManagers().remove(guild.getIdLong());

        event.getMessage().addReaction("\u23F9").queue();
    }
}

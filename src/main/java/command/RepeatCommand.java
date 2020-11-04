package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

public class RepeatCommand extends Command {

    NanoClient nanoClient;

    public RepeatCommand(NanoClient nanoClient) {
        this.name = "loop";
        this.help = "Re-enqueue the song after it finished playing";
        this.aliases = new String[]{"repeat"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        if (!nanoClient.getMusicService().isMemberInVoiceState(event.getMember())) {
            event.reply("Are you sure you are in voice channel ?");
            return;
        }

        if (musicManager.player.getPlayingTrack() == null) {
            event.reply("Not playing anything");
            return;
        }

        if (musicManager.scheduler.isInLoopState()) {
            musicManager.scheduler.setInLoopState(false);
            event.getMessage().addReaction("\u21AA").queue();
        }
        else {
            musicManager.scheduler.setInLoopState(true);
            event.getMessage().addReaction("\uD83D\uDD01").queue();
        }
    }
}

package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

public class DjModeCommand extends Command {

    private NanoClient nanoClient;

    public DjModeCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "dj";
        this.aliases = new String[] {"dj_mode"};
        this.help = "Enable or disable Dj mode.\n:warning: If dj mode is enabled, " +
                "only member with role DJ can request song or change_volume/pause/resume/skip/join/leave";
        this.guildOnly = true;
        this.cooldown = 2;

        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        if (musicManager.isInDjMode()) {
            musicManager.setInDjMode(false);
        }
        else {
            musicManager.setInDjMode(true);
        }
    }
}

package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

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

        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        if (!nanoClient.getMusicManagers().containsKey(guild.getIdLong())) {
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(guild);

        musicManager.player.stopTrack();
        musicManager.scheduler.getQueue().clear();
        nanoClient.getMusicManagers().remove(guild.getIdLong());

        event.getMessage().addReaction("\u23F9").queue();
    }
}

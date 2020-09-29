package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import service.Music.GuildMusicManager;

public class LeaveCommand extends Command {
    NanoClient nanoClient;

    public LeaveCommand(NanoClient nanoClient) {
        this.name = "leave";
        this.help = "Stop playing music and leaves voice channel";
        this.aliases = new String[]{"stop"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(guild);
        musicManager.player.stopTrack();
        musicManager.scheduler.getQueue().clear();
        nanoClient.getMusicManagers().remove(Long.parseLong(guild.getId()));

        event.getMessage().addReaction("\u23F9").queue();
    }
}
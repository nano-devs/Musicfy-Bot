package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.Music.GuildMusicManager;

public class PauseCommand extends Command {

    NanoClient nanoClient;

    public PauseCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "pause";
        this.guildOnly = true;
        this.cooldown = 1;
        this.help = "Pause current playing song";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setPaused(true);
        event.getMessage().addReaction("\u23F8").queue(); // Pause button
    }
}
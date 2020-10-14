package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class ResumeCommand extends Command {

    NanoClient nanoClient;

    public ResumeCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "resume";
        this.guildOnly = true;
        this.cooldown = 1;
        this.help = "Resume paused song\n";
        this.category = new Category("Music");
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setPaused(false);
        event.getMessage().addReaction("\u25B6").queue(); // Play Button
    }
}

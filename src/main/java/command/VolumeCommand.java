package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class VolumeCommand extends Command {

    NanoClient nanoClient;

    public VolumeCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "volume";
        this.aliases = new String[]{"vol", "volum", "v"};
        this.guildOnly = true;
        this.cooldown = 1;
        this.help = "Adjust volume number.";
        this.arguments = "<number>";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        try {
            // Try converting the string to integer
            int volume = Integer.parseInt(event.getArgs());
            // Check volume range
            if (volume < 0 || volume > 100) {
                event.reply("Invalid volume number, number has to be between 0 and 100");
                return;
            }
            musicManager.player.setVolume(volume);
        } catch (Exception e) {
            event.reply("Invalid volume number, command e.g. `.volume 25` to change volume to 25%");
            return;
        }
        event.reply("Volume " + event.getArgs() + "%");
    }
}

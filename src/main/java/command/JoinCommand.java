package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class JoinCommand extends Command {

    NanoClient nanoClient;

    public JoinCommand(NanoClient nanoClient) {
        this.name = "join";
        this.help = "Join user's voice channel";
        this.aliases = new String[]{"summon"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply("You're not connected to any voice channel");
            return;
        }
        event.getGuild().getAudioManager().openAudioConnection(channel);
        event.reply("Connected to " + channel.getName());
    }
}

package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ShuffleCommand extends Command {

    NanoClient nanoClient;

    public ShuffleCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "shuffle";
        this.aliases = new String[] {"shuffle_queue", "sq"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = this.nanoClient.getGuildAudioPlayer(event.getGuild());

        // Get current queue
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        // Add songs to List
        List<AudioTrack> tracks = new ArrayList<>();
        tracks.addAll(queue);

        Collections.shuffle(tracks);

        // Replace current queue
        musicManager.scheduler.getQueue().clear();
        musicManager.scheduler.getQueue().addAll(tracks);

        event.getMessage().addReaction(event.getClient().getSuccess()).queue();
    }
}

package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ShuffleCommand extends Command {

    NanoClient nanoClient;

    public ShuffleCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "shuffle";
        this.aliases = new String[] {"shuffle_queue"};
        this.help = "Shuffles song queue";
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel userVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (userVoiceChannel == null) {
            event.reply(":x: | You are not connected to any voice channel");
            return;
        }

        GuildMusicManager musicManager = this.nanoClient.getGuildAudioPlayer(event.getGuild());
        if (musicManager.scheduler.getQueue().size() <= 0) {
            return;
        }

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

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

package service.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

public class AudioOnLoadResultHandler implements AudioLoadResultHandler {

    private TextChannel channel;
    private String trackUrl;

    @Override
    public void trackLoaded(AudioTrack track) {
        channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        for (AudioTrack track : playlist.getTracks()) {

        }
        channel.sendMessage(
                String.valueOf(playlist.getTracks().size()) + " entries has been added to queue").queue();
    }

    @Override
    public void noMatches() {
        channel.sendMessage("Nothing found by " + trackUrl).queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Could not play: " + exception.getMessage()).queue();
    }
}

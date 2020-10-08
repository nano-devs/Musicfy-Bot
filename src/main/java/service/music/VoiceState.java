package service.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

public class VoiceState {

    private AudioTrack track;
    private User requester;

    public VoiceState(User requester, AudioTrack track) {
        this.requester = requester;
        this.track = track;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public void setTrack(AudioTrack track) {
        this.track = track;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }
}

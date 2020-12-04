package service.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private boolean inLoopState;
    public TextChannel textChannel;

    /**
     * Set of user id for the skip vote.
     */
    public Set<String> skipVoteSet;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.inLoopState = false;

        this.skipVoteSet = new HashSet<String>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        player.startTrack(queue.poll(), false);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public boolean isInLoopState() {
        return inLoopState;
    }

    public void setInLoopState(boolean inLoopState) {
        this.inLoopState = inLoopState;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.info("onTrackEnd " + track.getIdentifier() + " " + endReason.name());

        // Reset vote skip
        skipVoteSet.clear();

        // If repeat true, then re-enqueue url.
        if (inLoopState) {
            queue(track.makeClone());
        }

        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        } else {

        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.info("onTrackStart " + track.getIdentifier());

        super.onTrackStart(player, track);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.info("onTrackException " + track.getIdentifier() +
                " Message: " + exception.getMessage() +
                " Caused by: " + exception.getCause());

        textChannel.sendMessage(":x: | " + exception.getMessage() +
                ". Try request the song again, if it's still broke, please contact developers... :(").queue();

        exception.printStackTrace();

        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.info("onTrackStuck " + track.getIdentifier());

        super.onTrackStuck(player, track, thresholdMs);
    }
}

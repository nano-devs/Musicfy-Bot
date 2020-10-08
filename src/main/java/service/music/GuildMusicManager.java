package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jsoup.internal.StringUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;
    /**
     * Set of user id for the skip vote.
     */
    public Set<String> skipVoteSet;

    private int volume;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        skipVoteSet = new HashSet<String>();
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    /**
     *
     * @param event CommandEvent.
     */
    public void announceNowPlaying(CommandEvent event) {
        String voiceChannelName = event.getGuild().getAudioManager().getConnectedChannel().getName();
        String announcement = "\uD83D\uDCE2 Now Playing in " + voiceChannelName + " \uD83C\uDFB5";
        String description = "[" + player.getPlayingTrack().getInfo().title + "]("
                + player.getPlayingTrack().getInfo().uri + ")\n\n";
        description += getProgressBar() + "\n";
        User requester = player.getPlayingTrack().getUserData(User.class);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(announcement);
        embedBuilder.setAuthor(requester.getName(), requester.getAvatarUrl(), requester.getAvatarUrl());
        embedBuilder.setDescription(description);
        embedBuilder.setFooter("Source: " + player.getPlayingTrack().getInfo().author);

        embedBuilder.setColor(Color.MAGENTA);

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    /**
     * Get now playing description string.
     * @return String of now playing description
     */
    public String getNowPlayingDescription() {
        String description = "**[" + player.getPlayingTrack().getInfo().title + "]("
                + player.getPlayingTrack().getInfo().uri + ")**\n\n";
        description += getProgressBar() + "\n\n";
        return description;
    }

    private String getProgressBar() {
        long duration = player.getPlayingTrack().getDuration();
        long position = player.getPlayingTrack().getPosition();

        int playedPercentage = (int) (((double) position / (double) duration) * 100);
        int currentPostIndex = playedPercentage / 10;

        String[] progressBar = "\u25AC \u25AC \u25AC \u25AC \u25AC \u25AC \u25AC \u25AC \u25AC \u25AC".split(" ");
        progressBar[currentPostIndex] = "\uD83D\uDD18";
        return "\u25B6" + StringUtil.join(progressBar, "") + "`["
                + Utils.getDurationFormat(position) + "/" + Utils.getDurationFormat(duration)
                + "]` \uD83D\uDD0A \n";
    }

    public String getEstimatedTimeUntilPlaying(int entryNumber) {
        long totalDuration = 0;
        int counter = 0;
        for (AudioTrack queuedTrack : scheduler.getQueue()) {
            counter += 1;
            if (counter >= entryNumber)
                break;
            totalDuration += queuedTrack.getDuration();
        }
        totalDuration += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
        return Utils.getDurationFormat(totalDuration);
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
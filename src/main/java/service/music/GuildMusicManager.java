package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.GuildSetting;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import org.jsoup.internal.StringUtil;
import service.music.filter.BassBoostFilter;
import service.music.filter.FlatFilter;
import service.music.filter.PianoFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager extends GuildSetting implements GuildSettingsProvider {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;

    private int volume;

    private boolean waitingForUser = false;

    private ScheduledFuture<?> waitingFuture;

    private boolean pauseStatus = false;

    private final Set<String> prefixes;

    private final BassBoostFilter bassBoostFilter;

    private final PianoFilter pianoFilter;

    private final FlatFilter flatFilter;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        super();

        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);

        prefixes = new HashSet<>();

        bassBoostFilter = new BassBoostFilter();
        pianoFilter = new PianoFilter();
        flatFilter = new FlatFilter();
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
        String announcement = "\uD83C\uDFB5 Now Playing in :loud_sound: `" + voiceChannelName + "`";
        String description = "[" + player.getPlayingTrack().getInfo().title + "]("
                + player.getPlayingTrack().getInfo().uri + ")\n\n";
        description += getProgressBar() + "\n";
        Member requester = player.getPlayingTrack().getUserData(Member.class);

        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
        embedBuilder.setTitle(announcement);
        embedBuilder.setAuthor(requester.getUser().getName(),
                requester.getUser().getAvatarUrl(), requester.getUser().getAvatarUrl());
        embedBuilder.setDescription(description);

        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
        String footerMessage = "Source: " + player.getPlayingTrack().getInfo().author
                + " | Vote " + scheduler.skipVoteSet.size() + "/" + connectedMembers;
        embedBuilder.setFooter(footerMessage);

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

        String[] progressBar = "\u2501 \u2501 \u2501 \u2501 \u2501 \u2501 \u2501 \u2501 \u2501 \u2501".split(" ");
        progressBar[currentPostIndex] = "\uD83D\uDD18";

        String button = ":pause_button:";
        if (pauseStatus) {
            button = ":arrow_forward:";
        }

        return button + StringUtil.join(progressBar, "") + "`["
                + MusicUtils.getDurationFormat(position) + "/" + MusicUtils.getDurationFormat(duration)
                + "]` \uD83D\uDD0A \n";
    }

    public String getEstimatedTimeUntilPlaying(int entryNumber) {
        if (entryNumber == 0) {
            return "00:00";
        }
        long totalDuration = 0;
        int counter = 0;
        for (AudioTrack queuedTrack : scheduler.getQueue()) {
            counter += 1;
            if (counter >= entryNumber)
                break;
            totalDuration += queuedTrack.getDuration();
        }
        totalDuration += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
        return MusicUtils.getDurationFormat(totalDuration);
    }

    public boolean isPauseStatus() {
        return pauseStatus;
    }

    public void setPauseStatus(boolean pauseStatus) {
        this.pauseStatus = pauseStatus;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isWaitingForUser() {
        return waitingForUser;
    }

    public void setWaitingForUser(boolean waitingForUser) {
        this.waitingForUser = waitingForUser;
    }

    public ScheduledFuture<?> getWaitingFuture() {
        return waitingFuture;
    }

    public void setWaitingFuture(ScheduledFuture<?> waitingFuture) {
        this.waitingFuture = waitingFuture;
    }

    public boolean isQueueFull() {
        return this.scheduler.getQueue().size() >= this.maxQueueLength;
    }

    public void applyBassBoostFilter() {
        this.player.setFilterFactory(bassBoostFilter.getEqualizer());
    }

    public void applyPianoFilter() {
        this.player.setFilterFactory(pianoFilter.getEqualizer());
    }

    public void applyFlatFilter() {
        this.player.setFilterFactory(flatFilter.getEqualizer());
    }

    public void clearFilter() {
        this.player.setFilterFactory(null);
    }

    @Nullable
    @Override
    public Collection<String> getPrefixes() {
        if (this.customPrefix == null) {
            return null;
        }
        prefixes.add(this.customPrefix);
        return prefixes;
    }
}
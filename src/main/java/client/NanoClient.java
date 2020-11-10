package client;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.ClassicUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;
import service.music.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NanoClient implements GuildSettingsManager {
    private JDA jda;

    private final Map<Long, GuildMusicManager> musicManagers;
    private MusicService musicService;
    private final AudioPlayerManager playerManager;
    private EventWaiter waiter;

    public NanoClient(MusicService musicService, EventWaiter waiter) {
        this.musicService = musicService;

        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        this.waiter = waiter;
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);

            try {
                musicManager.loadSetting(guildId);

                if (!musicManager.canLoadSetting()) {
                    musicManager.loadDefaultSetting(guildId);
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    /**
     *
     * @param musicManager Music manager to load & play the url
     * @param channel Current text channel
     * @param trackUrl Given url. Supported URL: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo
     * @param requester User who request the song.
     */
    public void loadAndPlayUrl(GuildMusicManager musicManager, final TextChannel channel,
                               final String trackUrl, Member requester) {

        musicManager.scheduler.textChannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl,
                new AudioLoadResultHandlerUrl(musicManager, channel, trackUrl, requester));
    }

    /**
     *
     * @param musicManager Music manager to load & play the url
     * @param channel Current text channel
     * @param trackUrl Given url. Supported URL: Youtube, Twitch, SoundCloud, Bandcamp, Vimeo
     * @param requester User who request the song.
     */
    public void loadAndPlayUrl(GuildMusicManager musicManager, final TextChannel channel,
                               final String trackUrl, Member requester, int loadLimit) {

        musicManager.scheduler.textChannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (track.getDuration() > 900000) {
                    String errorMessage = ":negative_squared_cross_mark: | cannot load song with duration longer than 15 minutes";
                    channel.sendMessage(errorMessage).queue();
                    return;
                }
                track.setUserData(requester);
                musicManager.scheduler.queue(track);

                PremiumService.addHistory(track.getInfo().title, trackUrl, requester.getGuild(), requester.getUser());

                int positionInQueue = musicManager.scheduler.getQueue().size();

                if (channel != null) {
                    CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
                    embedBuilder.setDescription("\uD83C\uDFB5 [" + track.getInfo().title + "](" + track.getInfo().uri + ")");

                    embedBuilder.setAuthor("Added to queue", requester.getUser().getEffectiveAvatarUrl(),
                            requester.getUser().getEffectiveAvatarUrl());

                    embedBuilder.addField("Channel", track.getInfo().author, true);
                    embedBuilder.addField("Song Duration", MusicUtils.getDurationFormat(track.getDuration()), true);
                    embedBuilder.addField("Position in queue", "" + positionInQueue, true);
                    embedBuilder.addField("Estimated time until playing",
                            musicManager.getEstimatedTimeUntilPlaying(positionInQueue), true);

                    channel.sendMessage(embedBuilder.build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                int addedSize = 0;
                int index = 0;
                for (AudioTrack track : playlist.getTracks()) {
                    if (index == 0) {
                        index += 1;
                        continue;
                    }
                    if (track.getDuration() > 900000) {
                        continue;
                    }
                    track.setUserData(requester);
                    musicManager.scheduler.queue(track);
                    addedSize += 1;
                    if (musicManager.isQueueFull() || addedSize >= loadLimit) {
                        break;
                    }
                }

                PremiumService.addHistory(playlist.getName(), trackUrl, requester.getGuild(), requester.getUser());

                if (channel != null)
                    channel.sendMessage(":white_check_mark: | " + addedSize +
                            " entries from **"+ playlist.getName() + "** has been added to queue").queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage(":negative_squared_cross_mark: | Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(":negative_squared_cross_mark: | Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    /**
     *
     * @param musicManager Music manager to load & play the url
     * @param channel Current text channel
     * @param keywords Given keywords.
     * @param requester User, requester.
     */
    public void loadAndPlayKeywords(GuildMusicManager musicManager, final TextChannel channel,
                               final String keywords, Member requester) {

        musicManager.scheduler.textChannel = channel;

        playerManager.loadItemOrdered(musicManager, "ytsearch: " + keywords,
                new AudioLoadResultHandlerKeyword(musicManager, channel, keywords, requester));
    }

    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public Map<Long, GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public void setWaiter(EventWaiter waiter) {
        this.waiter = waiter;
    }

    public CustomEmbedBuilder getEmbeddedVoteLink(ClassicUser classicUser, CommandEvent event) {
        String voteUrl = "";
        String message = "[Vote]() & use **" + event.getClient().getPrefix() +
                "claim** command to claim rewards :>\n" + voteUrl;

        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
        embedBuilder.setTitle(":headphones: | Thank you for using " + event.getSelfUser().getName() + "!");
        embedBuilder.setAuthor(event.getAuthor().getName() + " Stocks",
                event.getAuthor().getEffectiveAvatarUrl(),
                event.getAuthor().getEffectiveAvatarUrl());
        embedBuilder.addField("Daily Quota", String.valueOf(classicUser.getDailyQuota()), true);
        embedBuilder.addField("Claimed Reward", String.valueOf(classicUser.getRecommendationQuota()), true);
        embedBuilder.addField("Increase your stocks :chart_with_upwards_trend: ", message, false);
        embedBuilder.setFooter("Have a nice dayy~");

        return embedBuilder;
    }

    @Nullable
    @Override
    public Object getSettings(Guild guild) {
        return this.getGuildAudioPlayer(guild);
    }
}

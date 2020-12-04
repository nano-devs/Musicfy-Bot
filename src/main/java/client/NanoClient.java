package client;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.client.config.RequestConfig;
import org.jetbrains.annotations.Nullable;
import service.music.*;

import java.sql.SQLException;
import java.util.Collections;
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
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setFilterHotSwapEnabled(true);

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        this.playerManager.setHttpRequestConfigurator((config) ->
                RequestConfig.copy(config).setConnectTimeout(10000).build());

        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);

        if (System.getenv("ipv6") != null) {
            String ipv6Block = System.getenv("ipv6block") + "/64";
            System.out.println("Setup ipv6: " + ipv6Block);
            new YoutubeIpRotatorSetup(
                    new NanoIpRoutePlanner(Collections.singletonList(new Ipv6Block(ipv6Block)), true))
                    .forSource(youtubeAudioSourceManager)
                    .setup();
        }
        this.playerManager.registerSourceManager(youtubeAudioSourceManager);
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));

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
                if (track.getDuration() > musicManager.getMaxSongDuration()) {
                    if (channel != null) {
                        String errorMessage = ":negative_squared_cross_mark: | Cannot load song with duration longer than 1 hour";
                        channel.sendMessage(errorMessage).queue();
                    }
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
                    if (track.getDuration() > musicManager.getMaxSongDuration()) {
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

                if (channel != null) {
                    CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();

                    embedBuilder.setDescription(":white_check_mark: | " + addedSize +
                            " entries from **" + playlist.getName() + "** has been added to queue");

                    embedBuilder.setAuthor("Added to queue", requester.getUser().getEffectiveAvatarUrl(),
                            requester.getUser().getEffectiveAvatarUrl());

                    embedBuilder.setFooter("Only song with duration less than 1 hour added to queue");
                    channel.sendMessage(embedBuilder.build()).queue();
                }
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

    @Nullable
    @Override
    public Object getSettings(Guild guild) {
        return this.getGuildAudioPlayer(guild);
    }
}

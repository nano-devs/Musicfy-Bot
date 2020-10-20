package client;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import service.music.GuildMusicManager;
import service.music.MusicService;
import service.music.MusicUtils;

import java.util.HashMap;
import java.util.Map;

public class NanoClient {
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

                int positionInQueue = musicManager.scheduler.getQueue().size();

                if (channel != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(requester.getColor());
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
                int skipCounter = 0;
                for (AudioTrack track : playlist.getTracks()) {
                    if (track.getDuration() > 900000) {
                        skipCounter += 1;
                        continue;
                    }
                    track.setUserData(requester);
                    musicManager.scheduler.queue(track);
                }
                if (channel != null)
                    channel.sendMessage(":white_check_mark: | " + String.valueOf(playlist.getTracks().size() - skipCounter) +
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
        playerManager.loadItemOrdered(musicManager, "ytsearch: " + keywords, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (track.getDuration() > 900000) {
                    String errorMessage = ":negative_squared_cross_mark: | cannot load song with duration longer than 15 minutes";
                    channel.sendMessage(errorMessage).queue();
                    return;
                }
                track.setUserData(requester);

                musicManager.scheduler.queue(track);

                int positionInQueue = musicManager.scheduler.getQueue().size();
                if (channel != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
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
                AudioTrack track = playlist.getSelectedTrack();
                if (track == null) {
                    track = playlist.getTracks().get(0);
                }
                if (track.getDuration() > 900000) {
                    String errorMessage = ":negative_squared_cross_mark: | cannot load song with duration longer than 15 minutes";
                    channel.sendMessage(errorMessage).queue();
                    return;
                }
                track.setUserData(requester);

                musicManager.scheduler.queue(track);

                if (channel != null) {
                    int positionInQueue = musicManager.scheduler.getQueue().size();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
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
            public void noMatches() {
                channel.sendMessage(":negative_squared_cross_mark: | Nothing found by " + keywords).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(":negative_squared_cross_mark: | Could not play: " + exception.getMessage()).queue();
            }
        });
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
}

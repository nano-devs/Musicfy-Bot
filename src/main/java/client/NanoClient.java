package client;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import service.Music.GuildMusicManager;
import service.Music.MusicService;

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
     * @param requester User, requester.
     */
    public void loadAndPlayUrl(GuildMusicManager musicManager, final TextChannel channel,
                               final String trackUrl, User requester) {
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                track.setUserData(requester);

                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    track.setUserData(requester);
                    musicManager.scheduler.queue(track);
                }
                channel.sendMessage(String.valueOf(playlist.getTracks().size()) +
                        " entries has been added to queue from " + playlist.getName()).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
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

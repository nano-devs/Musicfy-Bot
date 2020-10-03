package listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import service.music.GuildMusicManager;
import service.music.MusicService;

import java.util.HashMap;
import java.util.Map;

public class MusicMessageListener extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private MusicService musicService;

    public MusicMessageListener(MusicService musicService) {
        this.musicService = musicService;

        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String contentRaw = event.getMessage().getContentRaw();

        if (!contentRaw.startsWith(".")){
            return;
        }

        String[] command = contentRaw.split(" ", 2);

        if (".play".equals(command[0]) && command.length == 2) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            if (musicService.joinUserVoiceChannel(event)) {
                // Set default volume value
                musicManager.player.setVolume(15);
                musicService.loadAndPlay(playerManager, musicManager, event.getChannel(),
                        command[1], event.getAuthor());
            }
        } else if (".skip".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

            // Check if player is currently playing audio
            if (musicManager.player.getPlayingTrack() == null) {
                event.getChannel().sendMessage("Not playing anything").queue();
                return;
            }
            User requester = event.getAuthor();
            User nowPlayRequester = musicManager.player.getPlayingTrack().getUserData(User.class);
            if (requester.getId().equals(nowPlayRequester.getId())) {
                musicService.skipTrack(musicManager, event.getChannel());
                return;
            }
            musicManager.skipVoteSet.add(requester.getId());

            int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
            if (musicManager.skipVoteSet.size() > (connectedMembers - 1) / 2) {
                musicService.skipTrack(musicManager, event.getChannel());
                return;
            }
            event.getChannel().sendMessage(
                    "Vote: " + musicManager.skipVoteSet.size() + "/" + connectedMembers).queue();
        } else if (".join".equals(command[0])) {
            VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
            if (voiceChannel == null) {
                event.getChannel().sendMessage("Are you sure you're in voice channel ?").queue();
                return;
            }
            musicService.joinVoiceChannel(event.getGuild(), voiceChannel);
            event.getChannel().sendMessage("Connected").queue();
        } else if (".leave".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

            musicService.leaveVoiceChannel(event.getGuild(), musicManager);
            event.getChannel().sendMessage("Leave Voice Channel").queue();
            musicManagers.remove(Long.parseLong(event.getGuild().getId()));
        } else if (".volume".equals((command[0]))) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            int volume = 10;
            try {
                volume = Integer.parseInt(command[1]);
                musicManager.player.setVolume(volume);
            } catch (Exception e) {
                event.getChannel().sendMessage(
                        "Invalid volume number, example usage `.volume 25` to change volume to 25%").queue();
                return;
            }
            event.getChannel().sendMessage("Volume " + command[1] + "%").queue();
        } else if (".pause".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            musicManager.player.setPaused(true);
            event.getMessage().addReaction("\uD83D\uDC4C").queue();
        } else if (".resume".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            musicManager.player.setPaused(false);
            event.getMessage().addReaction("\uD83D\uDC4C").queue();
        } else if (".queue".equals(command[0])) {

        } else if (".np".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            AudioTrack nowPlayingTrack = musicManager.player.getPlayingTrack();
            String title = nowPlayingTrack.getInfo().title;
            long duration = nowPlayingTrack.getDuration();
            event.getChannel().sendMessage("Now playing: " + title + " by " +
                    nowPlayingTrack.getInfo().author).queue();
        }

        super.onGuildMessageReceived(event);
    }
}

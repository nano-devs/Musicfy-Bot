package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicService {
    public void loadAndPlay(AudioPlayerManager playerManager, GuildMusicManager musicManager,
                            final TextChannel channel, final String trackUrl, User requester) {
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

    public void loadAndPlayKeywords(AudioPlayerManager playerManager, GuildMusicManager musicManager,
                            final TextChannel channel, final String keywords, User requester) {
        playerManager.loadItemOrdered(musicManager, "ytsearch: " + keywords, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                track.setUserData(requester);

                play(musicManager, track);
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
                channel.sendMessage("Nothing found by " + keywords).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    public void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public void skipTrack(GuildMusicManager musicManager, TextChannel channel) {
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    public void adjustVolume(GuildMusicManager musicManager, int volume) {
        musicManager.player.setVolume(volume);
    }

    public void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    public boolean joinUserVoiceChannel(GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            event.getChannel().sendMessage("Are you sure you're in voice channel ?").queue();
            return false;
        }
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
        return true;
    }

    public boolean joinUserVoiceChannel(CommandEvent event) {
        Member member = event.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            event.reply("Are you sure you're in voice channel ?");
            return false;
        }
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
        return true;
    }

    public void joinVoiceChannel(Guild guild, VoiceChannel voiceChannel){
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
    }

    public void leaveVoiceChannel(Guild guild, GuildMusicManager musicManager){
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        musicManager.player.stopTrack();
        musicManager.scheduler.getQueue().clear();
    }

    public boolean isMemberInVoiceState(Member member) {
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            return false;
        }
        return true;
    }
}

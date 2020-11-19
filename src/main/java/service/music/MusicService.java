package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.ClassicUser;
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

        musicManager.player.destroy();
        musicManager.scheduler.getQueue().clear();
    }

    public boolean isMemberInVoiceState(Member member) {
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            return false;
        }
        return true;
    }

    public static CustomEmbedBuilder getEmbeddedVoteLink(ClassicUser classicUser, CommandEvent event) {
        String voteUrl = "";
        String message = "[Vote](https://top.gg/bot/473023109666963467) Muscify to claim rewards! :>\n" + voteUrl;

        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
        embedBuilder.setTitle(":headphones: | Thank you for using " + event.getSelfUser().getName() + "!");
        embedBuilder.setAuthor(event.getAuthor().getName() + " Stocks",
                event.getAuthor().getEffectiveAvatarUrl(),
                event.getAuthor().getEffectiveAvatarUrl());
        embedBuilder.addField("Daily Quota", String.valueOf(classicUser.getDailyQuota()), true);
        embedBuilder.addField("Vote Reward(s)", String.valueOf(classicUser.getRecommendationQuota()), true);
        embedBuilder.addField(":chart_with_upwards_trend: Increase your stocks", message, false);
        embedBuilder.setFooter("Have a nice dayy~");

        return embedBuilder;
    }
}

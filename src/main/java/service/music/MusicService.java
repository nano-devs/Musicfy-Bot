package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.ClassicUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicService {
    private static final Logger log = LoggerFactory.getLogger(MusicService.class);

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

    public boolean joinMemberVoiceChannel(GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            event.getChannel().sendMessage("Are you sure you're in voice channel ?").queue();
            return false;
        }
        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        try {
            audioManager.openAudioConnection(voiceChannel);
        } catch (IllegalArgumentException | InsufficientPermissionException joinException) {
            log.error(joinException.getMessage());
            return false;
        }
        return true;
    }

    public VoiceChannel joinMemberVoiceChannel(CommandEvent event) {
        // Redundant Code: check member voice state
        Member member = event.getMember();
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null){
            event.reply(":x: | You are not connected to any voice channel");
            return null;
        }

        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        try {
            audioManager.openAudioConnection(voiceChannel);
        } catch (IllegalArgumentException | InsufficientPermissionException joinException) {
            log.error(joinException.getMessage());
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":warning: Missing Permission: VOICE_CONNECT",
                    "I don't have permission to connect to voice channel",
                    true);
            event.reply(embedBuilder.build());
            return null;
        }

        return voiceChannel;
    }

    public boolean joinVoiceChannel(Guild guild, VoiceChannel voiceChannel){
        AudioManager audioManager = guild.getAudioManager();
        try {
            audioManager.openAudioConnection(voiceChannel);
        } catch (IllegalArgumentException | InsufficientPermissionException joinException) {
            log.error(joinException.getMessage());
            return false;
        }
        return true;
    }
    
    public void leaveVoiceChannel(Guild guild, GuildMusicManager musicManager){
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();

        musicManager.scheduler.setInLoopState(false);
        musicManager.scheduler.getQueue().clear();
        musicManager.player.destroy();
    }

    public boolean isMemberConnectedToVoice(Member member) {
        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        return voiceChannel != null;
    }

    /**
     * Check member's voice state, invoke before using command, used by some music commands.
     * @param event
     * @return
     */
    public boolean ensureVoiceState(CommandEvent event) {
        if (!this.isMemberConnectedToVoice(event.getMember())) {
            event.reply(":x: | You are not connected to any voice channel");
            return false;
        }
        else {
            VoiceChannel selfVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

            if (selfVoiceChannel == null) {
                selfVoiceChannel = this.joinMemberVoiceChannel(event);
                return selfVoiceChannel != null;
            }
            // Check user's voice channel.
            else if (!selfVoiceChannel.getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
                event.reply(":x: | You are not connected to **my voice channel**");
                return false;
            }
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

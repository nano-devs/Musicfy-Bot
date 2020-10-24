package listener;

import client.NanoClient;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import service.music.GuildMusicManager;

import java.util.concurrent.*;

public class MemberVoiceListener extends ListenerAdapter {

    NanoClient nanoClient;
    ScheduledExecutorService exec;

    public MemberVoiceListener(NanoClient nanoClient, ScheduledExecutorService exec) {
        this.nanoClient = nanoClient;
        this.exec = exec;
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel clientVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

        // Ignore if bot event.
        if (event.getMember().getUser().isBot()) {
            return;
        }

        // Ignore if not connected to any voice channel
        if (clientVoiceChannel == null) {
            return;
        }

        // Ignore if the event is from another voice channel.
        if (!event.getChannelLeft().getId().equals(clientVoiceChannel.getId())) {
            return;
        }

        if (!isThereAnyMemberIn(clientVoiceChannel)) {
            GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
            musicManager.setWaitingForUser(true);
            musicManager.player.setPaused(true);

            ScheduledFuture future = exec.schedule(() -> {
                if (musicManager.isWaitingForUser()) {
                    nanoClient.getMusicService().leaveVoiceChannel(event.getGuild(), musicManager);
                    nanoClient.getMusicManagers().remove(Long.parseLong(event.getGuild().getId()));
                }
            }, 10, TimeUnit.SECONDS);

            musicManager.setWaitingFuture(future);
        }

        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        VoiceChannel clientVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

        // Ignore if bot event.
        if (event.getMember().getUser().isBot() &&
                !event.getMember().getId().equals(nanoClient.getJda().getSelfUser().getId())) {
            return;
        }

        // Ignore if not connected to any voice channel
        if (clientVoiceChannel == null) {
            return;
        }

        // Ignore if the left/join event is from another voice channel.
        if (!event.getChannelJoined().getId().equals(clientVoiceChannel.getId()) &&
            !event.getChannelLeft().getId().equals(clientVoiceChannel.getId())) {
            return;
        }

        // if there is no member in connected voice channel.
        if (!isThereAnyMemberIn(clientVoiceChannel)) {
            GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
            musicManager.setWaitingForUser(true);
            musicManager.player.setPaused(true);

            ScheduledFuture future = exec.schedule(() -> {
                if (musicManager.isWaitingForUser()) {
                    nanoClient.getMusicService().leaveVoiceChannel(event.getGuild(), musicManager);
                    nanoClient.getMusicManagers().remove(Long.parseLong(event.getGuild().getId()));
                }
            }, 10, TimeUnit.SECONDS);

            musicManager.setWaitingFuture(future);
        }
        // When a member joined voice channel.
        else {
            GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
            if (musicManager.isWaitingForUser()) {
                System.out.println("RESUME & SET WAITING FALSE");
                musicManager.setWaitingForUser(false);
                musicManager.player.setPaused(false);
                musicManager.getWaitingFuture().cancel(true);
            }
        }
        super.onGuildVoiceMove(event);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        VoiceChannel clientVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

        // Ignore if bot event.
        if (event.getMember().getUser().isBot()) {
            return;
        }

        // Ignore event, If client is not connected to any voice channel.
        if (clientVoiceChannel == null) {
            return;
        }

        // Ignore join event to other voice channel.
        if (!clientVoiceChannel.getId().equals(event.getChannelJoined().getId())) {
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        if (musicManager.isWaitingForUser()) {
            musicManager.setWaitingForUser(false);
            musicManager.player.setPaused(false);
            musicManager.getWaitingFuture().cancel(true);
        }

        super.onGuildVoiceJoin(event);
    }

    private boolean isThereAnyMemberIn(VoiceChannel channel) {
        boolean foundMemberFlag = false;
        for (Member member : channel.getMembers()) {
            if (!member.getUser().isBot()) {
                foundMemberFlag = true;
                break;
            }
        }
        return foundMemberFlag;
    }
}

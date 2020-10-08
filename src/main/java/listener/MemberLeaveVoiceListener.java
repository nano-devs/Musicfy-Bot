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

public class MemberLeaveVoiceListener extends ListenerAdapter {

    NanoClient nanoClient;

    public MemberLeaveVoiceListener(NanoClient nanoClient) {
        this.nanoClient = nanoClient;
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel clientVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

        // Ignore if bot event.
        if (event.getMember().getUser().isBot()) {
            System.out.println("Bot event");
            return;
        }

        // Ignore if not connected to any voice channel
        if (clientVoiceChannel == null) {
            System.out.println("not connected");
            return;
        }

        // Ignore if the event is from another voice channel.
        if (!event.getChannelLeft().getId().equals(clientVoiceChannel.getId())) {
            System.out.println("another vc event");
            return;
        }

        if (!isThereAnyMemberIn(clientVoiceChannel)) {
            System.out.println("PAUSE & WAITING");
            GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
            musicManager.setWaitingForUser(true);
            musicManager.player.setPaused(true);

            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

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
            System.out.println("Bot event");
            return;
        }

        // Ignore if not connected to any voice channel
        if (clientVoiceChannel == null) {
            System.out.println("client not connected");
            return;
        }

        // Ignore if the left/join event is from another voice channel.
        if (!event.getChannelJoined().getId().equals(clientVoiceChannel.getId()) &&
            !event.getChannelLeft().getId().equals(clientVoiceChannel.getId())) {
            System.out.println("another vc event");
            return;
        }

        // if there is no member in connected voice channel.
        if (!isThereAnyMemberIn(clientVoiceChannel)) {
            System.out.println("PAUSE & WAITING");
            GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
            musicManager.setWaitingForUser(true);
            musicManager.player.setPaused(true);

            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
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
            System.out.println("Bot event");
            return;
        }

        // Ignore event, If client is not connected to any voice channel.
        if (clientVoiceChannel == null) {
            System.out.println("client not connected");
            return;
        }

        // Ignore join event to other voice channel.
        if (!clientVoiceChannel.getId().equals(event.getChannelJoined().getId())) {
            System.out.println("Other VC join event");
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

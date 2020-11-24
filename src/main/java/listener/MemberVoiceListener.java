package listener;

import client.NanoClient;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;

import java.util.concurrent.*;
import java.util.regex.Pattern;

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
        // if self join voice channel
        if (event.getMember().getId().equals(event.getGuild().getSelfMember().getId())) {
            if (event.getGuild().getSelfMember().hasPermission(Permission.VOICE_DEAF_OTHERS)) {
                event.getMember().deafen(true).queue();
            }
            else {
                TextChannel textChannel = nanoClient.getGuildAudioPlayer(event.getGuild()).scheduler.textChannel;

                CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
                embedBuilder.addField(":warning: Missing Permission: `Deafen Members`!",
                        "Please don't undeafen me! I work better by being deafened because: " +
                                "Less lag, more clear, better quality.",
                        true);

                textChannel.sendMessage(embedBuilder.build()).queue();
            }
            return;
        }

        VoiceChannel clientVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

        // Ignore if bot event.
        if (event.getMember().getUser().isBot()) {
            return;
        }

        // Ignore event, If client is not connected to any voice channel.
        if (clientVoiceChannel == null) {
            return;
        }

        // Ignore join event from other voice channel.
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

    @Override
    public void onGuildVoiceDeafen(@NotNull GuildVoiceDeafenEvent event) {
        super.onGuildVoiceDeafen(event);

        // if other users try to undeafen bot.
        if (!event.isDeafened() && event.getMember().getId().equals(event.getGuild().getSelfMember().getId())) {
            if (event.getMember().hasPermission(Permission.VOICE_DEAF_OTHERS)) {
                event.getGuild().getSelfMember().deafen(true).queue();
            }
//            else {
//                TextChannel textChannel = nanoClient.getGuildAudioPlayer(event.getGuild()).scheduler.textChannel;
//
//                CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
//                embedBuilder.addField(":warning: Missing Permission: `Deafen Members`!",
//                        "Please don't undeafen me! I work better by being deafened because: " +
//                                "Less lag, more clear, better quality, and doesn't randomly disconnect",
//                        true);
//
//                textChannel.sendMessage(embedBuilder.build()).queue();
//            }
        }
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

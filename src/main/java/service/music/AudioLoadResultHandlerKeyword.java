package service.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class AudioLoadResultHandlerKeyword implements AudioLoadResultHandler {
    GuildMusicManager musicManager;
    TextChannel channel;
    String keywords;
    Member requester;

    public AudioLoadResultHandlerKeyword(GuildMusicManager musicManager, TextChannel channel, String keywords, Member requester) {
        this.musicManager = musicManager;
        this.channel = channel;
        this.keywords = keywords;
        this.requester = requester;
    }

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

        PremiumService.addHistory(track.getInfo().title, track.getInfo().uri,
                requester.getGuild(), requester.getUser());

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
        for (AudioTrack track : playlist.getTracks()) {
            if (track.getDuration() > musicManager.getMaxSongDuration()) {
                continue;
            }
            track.setUserData(requester);

            musicManager.scheduler.queue(track);

            PremiumService.addHistory(track.getInfo().title, track.getInfo().uri,
                    requester.getGuild(), requester.getUser());

            if (channel != null) {
                int positionInQueue = musicManager.scheduler.getQueue().size();

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
            return;
        }

        if (channel != null) {
            String errorMessage = ":negative_squared_cross_mark: | cannot load song with duration longer than 1 hour";
            channel.sendMessage(errorMessage).queue();
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
}

package service.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class KeywordAudioLoadResultHandler implements AudioLoadResultHandler {
    GuildMusicManager musicManager;
    TextChannel channel;
    String keywords;
    Member requester;

    public KeywordAudioLoadResultHandler(GuildMusicManager musicManager, TextChannel channel, String keywords, Member requester) {
        this.musicManager = musicManager;
        this.channel = channel;
        this.keywords = keywords;
        this.requester = requester;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (channel != null && track.getDuration() > 900000) {
            String errorMessage = ":negative_squared_cross_mark: | cannot load song with duration longer than 15 minutes";
            channel.sendMessage(errorMessage).queue();
            return;
        }
        track.setUserData(requester);

        musicManager.scheduler.queue(track);

        PremiumService.addHistory(track.getInfo().title, track.getInfo().uri,
                requester.getGuild(), requester.getUser());

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

        PremiumService.addHistory(playlist.getName(), track.getInfo().uri,
                requester.getGuild(), requester.getUser());

        if (channel != null) {
            int positionInQueue = musicManager.scheduler.getQueue().size();

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
    public void noMatches() {
        channel.sendMessage(":negative_squared_cross_mark: | Nothing found by " + keywords).queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        channel.sendMessage(":negative_squared_cross_mark: | Could not play: " + exception.getMessage()).queue();
    }
}

package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class ShowQueueCommand extends Command {

    NanoClient nanoClient;

    public ShowQueueCommand(NanoClient nanoClient) {
        this.name = "queue";
        this.help = "Show song queue & current guild voice state\n";
        this.aliases = new String[]{"show_queue", "show queue", "q", "state"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.cooldown = 4;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        // Ensure Voice
        if (!nanoClient.getMusicService().isMemberInVoiceState(event.getMember())) {
            event.reply("Are you sure you are in voice channel ?");
            return;
        }
        if (musicManager.player.getPlayingTrack() == null) {
            event.reply("Not playing anything");
            return;
        }

        Paginator paginator = getPaginatedQueue(event, musicManager);
        paginator.paginate(event.getChannel(), 1);
    }

    private Paginator.Builder createPaginator() {
        return new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(7)
                .showPageNumbers(true)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .setFinalAction(message -> {
                    try {
                        message.clearReactions().queue();
                    } catch(PermissionException ex) {
                        message.delete().queue();
                    }
                })
                .setEventWaiter(nanoClient.getWaiter())
                .setTimeout(30, TimeUnit.SECONDS);
    }

    private Paginator getPaginatedQueue(CommandEvent event, GuildMusicManager musicManager) {
        String connectVoiceName = event.getGuild().getAudioManager().getConnectedChannel().getName();

        Paginator.Builder paginatorBuilder = createPaginator();

        paginatorBuilder.setText("\uD83C\uDFB6 | Now playing in :loud_sound: `" + connectVoiceName
                + "`\n**" + musicManager.player.getPlayingTrack().getInfo().title
                + " by " + musicManager.player.getPlayingTrack().getInfo().author  + "**");

        if (musicManager.scheduler.getQueue().size() > 0) {
            int counter = 0;
            String[] songs = new String[musicManager.scheduler.getQueue().size()];
            AudioTrack tempTrack = null;

            // first entry, estimation time until playing.
            long total = musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition();
            for (AudioTrack track : musicManager.scheduler.getQueue()) {
                if (tempTrack != null) {
                    total += tempTrack.getDuration();
                }
                String queueValue = "**" + track.getInfo().title
                        + "** by **" + track.getInfo().author + "**\n";
                queueValue += "Requested by **" + track.getUserData(Member.class).getUser().getName() + "** | ";
                queueValue += "Estimated time until playing **" + MusicUtils.getDurationFormat(total) + "**";
                songs[counter] = queueValue;
                counter++;
                tempTrack = track;
            }
            paginatorBuilder.setItems(songs);
        }
        else {
            paginatorBuilder.useNumberedItems(false);
            paginatorBuilder.setItems("**Empty queue**");
        }

        paginatorBuilder.setColor(event.getMember().getColor());

        paginatorBuilder.setUsers(event.getAuthor());

        return paginatorBuilder.build();
    }

    private EmbedBuilder getRichEmbeddedQueue(CommandEvent event, GuildMusicManager musicManager) {

        // Build embedded message
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(event.getMember().getColor());

        // Title
        embedBuilder.setTitle("**" + event.getGuild().getName() + "**'s Queue");

        // Contents
        embedBuilder.addField("\uD83C\uDFB6 | Now playing", musicManager.getNowPlayingDescription(), false);

        // Volume Info
        String volumeFieldName = " | volume";
        int volume = musicManager.player.getVolume();
        if (volume > 66) {
            volumeFieldName = "\uD83D\uDD0A" + volumeFieldName;
        }
        else if (volume > 33) {
            volumeFieldName = "\uD83D\uDD09" + volumeFieldName;
        }
        else {
            volumeFieldName = "\uD83D\uDD08" + volumeFieldName;
        }
        embedBuilder.addField(volumeFieldName, volume + "%", true);

        // TrackScheduler Repeat State
        String repeatState = "On";
        if (!musicManager.scheduler.isInLoopState()) {
            repeatState = "Off";
        }
        embedBuilder.addField("\uD83D\uDD01 | Repeat", repeatState, true);

        // Queue Values
        String queueValue = "";
        int counter = 1;
        for (AudioTrack track : musicManager.scheduler.getQueue()) {
            queueValue += "[" + counter + "]. **" + track.getInfo().title
                    + "** by **" + track.getInfo().author + "**\n";
            queueValue += "Requested by **" + track.getUserData(Member.class).getUser().getName() + "**\n";

            counter += 1;
            if (counter >= 7) {
                break;
            }
        }
        if (queueValue.equals("")) {
            queueValue = "*Empty queue*";
        }
        embedBuilder.addField("\uD83C\uDFB6 | The First 7 Entries in Queue", queueValue, false);

        // Footer
        // MIGHT BE BUGGY, CONCEPT MUST RE-CALCULATED EVERYTIME VOICE CHANNEL TRIGGER EVENT.
        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1;
        String footerValue = event.getMember().getEffectiveName() + " can skip current song | "
                + musicManager.skipVoteSet.size() + "/" + connectedMembers
                + " skip votes";
        embedBuilder.setFooter(footerValue);

        return embedBuilder;
    }
}

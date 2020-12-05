package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.PermissionException;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class ShowPaginatedQueueCommand extends Command {

    NanoClient nanoClient;

    public ShowPaginatedQueueCommand(NanoClient nanoClient) {
        this.name = "queue";
        this.help = "Show paginated song queue";
        this.aliases = new String[]{"show_queue", "q", "sq", "pq"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.cooldown = 4;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        // Ensure Voice
        if (!nanoClient.getMusicService().ensureVoiceState(event)) {
            return;
        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
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

        paginatorBuilder.setColor(new Color(211, 0, 137));

        paginatorBuilder.setUsers(event.getAuthor());

        return paginatorBuilder.build();
    }
}

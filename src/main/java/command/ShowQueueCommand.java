package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import service.music.GuildMusicManager;

import java.awt.*;

public class ShowQueueCommand extends Command {

    NanoClient nanoClient;

    public ShowQueueCommand(NanoClient nanoClient) {
        this.name = "queue";
        this.help = "Show song queue & current guild voice state";
        this.aliases = new String[]{"show_queue", "show queue", "q", "state"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.cooldown = 2;
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

        // Build embedded message
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.MAGENTA);

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
            queueValue += "Requested by **" + track.getUserData(User.class).getName() + "**\n";

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
        // MIGHT BE BUGGY, CONCEPT MUST RE-CALCULATE VOTE EVERYTIME VOICE CHANNEL TRIGGER EVENT.
        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1;
        String footerValue = event.getMember().getEffectiveName() + " can skip current song | "
                            + musicManager.skipVoteSet.size() + "/" + connectedMembers
                            + " skip votes";
        embedBuilder.setFooter(footerValue);

        // Build & Send embed.
        event.reply(embedBuilder.build());
    }
}

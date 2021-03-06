package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

public class ShowGuildStateCommand extends Command {

    NanoClient nanoClient;

    public ShowGuildStateCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "guild_state";
        this.help = "Show current audio player state and top 7 tracks in queue";
        this.aliases = new String[]{"show_guild_state", "gs", "state"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getGuild().getAudioManager().getConnectedChannel();
        if (channel == null) {
            event.reply("Not playing anything");
            return;
        }
        // Ensure Voice
//        if (!nanoClient.getMusicService().ensureVoiceState(event)) {
//            return;
//        }

        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        if (musicManager.player.getPlayingTrack() == null) {
            event.reply(":x: | Not playing anything");
            return;
        }

        event.reply(getRichEmbeddedQueue(event, musicManager).build());
    }

    private CustomEmbedBuilder getRichEmbeddedQueue(CommandEvent event, GuildMusicManager musicManager) {

        // Build embedded message
        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();

        // Title
        embedBuilder.setTitle("**" + event.getGuild().getName() + "**'s Queue");

        embedBuilder.setThumbnail(event.getGuild().getIconUrl());

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
        embedBuilder.addField("\uD83C\uDFB6 | Top 7 Entries in Queue", queueValue, false);

        // Footer
        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1;
        String footerValue = event.getMember().getEffectiveName() + " can skip current song | "
                + musicManager.scheduler.skipVoteSet.size() + "/" + connectedMembers
                + " skip votes";
        embedBuilder.setFooter(footerValue);

        return embedBuilder;
    }
}

package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import service.Music.GuildMusicManager;

public class SkipCommand extends Command {

    NanoClient nanoClient;

    public SkipCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "skip";
        this.aliases = new String[]{"next", "next_track", "play_next", "skip_next"};
        this.guildOnly = true;
        this.help = "Vote skip for current playing song";
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        // Check if player is currently playing audio
        if (musicManager.player.getPlayingTrack() == null) {
            event.getChannel().sendMessage("Not playing anything").queue();
            return;
        }
        User requester = event.getAuthor();
        User nowPlayRequester = musicManager.player.getPlayingTrack().getUserData(User.class);
        if (requester.getId().equals(nowPlayRequester.getId())) {
            musicManager.scheduler.nextTrack();
            event.getMessage().addReaction("U+23ED").queue();
            return;
        }
        musicManager.skipVoteSet.add(requester.getId());

        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
        if (musicManager.skipVoteSet.size() > (connectedMembers - 1) / 2) {
            musicManager.scheduler.nextTrack();
            event.getMessage().addReaction("U+23ED").queue();
            return;
        }
        event.getChannel().sendMessage(
                "Vote: " + musicManager.skipVoteSet.size() + "/" + connectedMembers).queue();
    }
}

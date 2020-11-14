package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class SkipCommand extends Command {

    NanoClient nanoClient;

    public SkipCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.name = "skip";
        this.aliases = new String[]{"next", "next_track", "play_next", "skip_next"};
        this.guildOnly = true;
        this.help = "Vote skip for current playing song";
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply(":x: | You are not connected to any voice channel");
            return;
        }

        // Check if player is currently playing audio
        if (musicManager.player.getPlayingTrack() == null) {
            event.getChannel().sendMessage(":x: | Not playing anything").queue();
            return;
        }

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }

        User requester = event.getAuthor();

        Member nowPlayRequester = musicManager.player.getPlayingTrack().getUserData(Member.class);
        if (requester.getId().equals(nowPlayRequester.getId())) {
            musicManager.scheduler.nextTrack();
            musicManager.scheduler.skipVoteSet.clear();
            event.getMessage().addReaction("U+23ED").queue();
            return;
        }
        musicManager.scheduler.skipVoteSet.add(requester.getId());

        int connectedMembers = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
        if (musicManager.scheduler.skipVoteSet.size() > (connectedMembers - 1) / 2) {
            musicManager.scheduler.nextTrack();
            musicManager.scheduler.skipVoteSet.clear();
            event.getMessage().addReaction("U+23ED").queue();
            return;
        }
        event.getChannel().sendMessage(
                ":white_check_mark: | Vote: " + musicManager.scheduler.skipVoteSet.size()
                        + "/" + connectedMembers).queue();
    }
}

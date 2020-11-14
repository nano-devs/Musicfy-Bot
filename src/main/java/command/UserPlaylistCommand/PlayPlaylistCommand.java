package command.UserPlaylistCommand;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

import java.util.ArrayList;

public class PlayPlaylistCommand extends UserPlaylistBaseCommand
{
    private final NanoClient nano;

    public PlayPlaylistCommand(NanoClient nano)
    {
        this.nano = nano;

        this.name = "play_my_playlist";
        this.aliases = new String[]{"pmp"};
        this.arguments = "<playlist name>";
        this.help = "Play all track from the user's own playlist.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        VoiceChannel userVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (userVoiceChannel == null)
        {
            event.reply(":x: | You are not connected to any voice channel");
            return;
        }

        if (event.getSelfMember().getVoiceState().getChannel() == null)
        {
            event.getGuild().getAudioManager().openAudioConnection(userVoiceChannel);
        }

        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Invalid argument, please provide the playlist name that you want to play.",
                    true);
            return;
        }

        PlaylistModel db = new PlaylistModel();

        if (!db.isPlaylistNameExist(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist `" + event.getArgs().trim() + "` does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table);

        if (tracks.size() <= 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist `" + event.getArgs().trim() + "` is empty.",
                    true);
            event.reply(embed.build());
        }
        else
        {
            GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());

            if (musicManager.isInDjMode()) {
                if (!MusicUtils.hasDjRole(event.getMember())) {
                    event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                    return;
                }
            }

            int addedSize = 0;
            for (int i = 0; i < tracks.size(); i++)
            {
                this.nano.loadAndPlayUrl(musicManager, null, tracks.get(i).url, event.getMember());
                addedSize += 1;
                if (musicManager.isQueueFull()) {
                    break;
                }
            }
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Added " + addedSize + " song(s) to queue.",
                    true);
            embed.setFooter("Only song with duration less than 1 hour has been added to queue");
            event.reply(embed.build());
        }
    }
}

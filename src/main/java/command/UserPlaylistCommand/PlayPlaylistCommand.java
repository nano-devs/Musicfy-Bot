package command.UserPlaylistCommand;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.util.ArrayList;

public class PlayPlaylistCommand extends UserPlaylistBaseCommand
{
    private final NanoClient nano;

    public PlayPlaylistCommand(NanoClient nano)
    {
        this.nano = nano;

        this.name = "play_user_playlist";
        this.aliases = new String[]{"pup"};
        this.arguments = "<playlist name>";
        this.help = "Play all track from the user's own playlist.\n";
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

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give playlist name you want to to play.",
                    true);
            return;
        }

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There are no playlist with `" + event.getArgs().trim() + "` names.",
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
                    "There are no track in `" + event.getArgs().trim() + "` playlist.",
                    true);
            event.reply(embed.build());
        }
        else
        {
            GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());
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
                    "Added " + addedSize + " track(s) to the queue.",
                    true);
            embed.setFooter("Only song with duration less than 15 minutes added to queue");
            event.reply(embed.build());
        }
    }
}

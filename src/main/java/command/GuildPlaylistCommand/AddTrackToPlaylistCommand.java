package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.TrackModel;
import net.dv8tion.jda.api.EmbedBuilder;

public class AddTrackToPlaylistCommand extends GuildPlaylistBaseCommand
{
    private final int maxTrack = 20;

    public AddTrackToPlaylistCommand()
    {
        this.name = "add_track_to_guild_playlist";
        this.aliases = new String[]{"attgp"};
        this.arguments = "<playlist name>, <track title>, <url>";
        this.help = "Add a new track to guild playlist. \n" +
                    "Use coma (,) as separator for each arguments.";
        this.cooldown = 2;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getArgs().split(",").length != 3)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Invalid given arguments.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().split(",")[0].trim();
        String title = event.getArgs().split(",")[1].trim();
        String url = event.getArgs().split(",")[2].trim();

        TrackModel track = new TrackModel();
        long trackId = track.getTrackId(url);
        if (trackId <= 0)
        {
            track.addTrack(title, url);
        }
        trackId = track.getTrackId(url);

        PlaylistModel db = new PlaylistModel();
        if (db.isPlaylistNameAvailable(event.getGuild().getIdLong(), playlistName, this.table))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Playlist `" + playlistName + "` not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (db.countPlaylistTrack(db.getPlaylistId(event.getGuild().getIdLong(), playlistName, this.table), this.table) >= this.maxTrack)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Track for playlist have reached maximum limit.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (db.addTrackToPlaylist(event.getGuild().getIdLong(), playlistName, trackId, this.table))
        {
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Track `" + title + "` added to playlist `" + playlistName + "`.",
                    true);
        }
        else
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Can't add track `" + title + "` to playlist `" + playlistName + "`.",
                    true);
        }
        event.reply(embed.build());
    }
}

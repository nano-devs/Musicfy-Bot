package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumModel;
import database.TrackModel;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AddTrackToPlaylistCommand extends UserPlaylistBaseCommand
{
    private final int maxTrack = 20;

    public AddTrackToPlaylistCommand()
    {
        this.name = "add_track_to_user_playlist";
        this.aliases = new String[]{"attup"};
        this.arguments = "<playlist name>, <track title>, <url>";
        this.help = "Add a new track to user playlist. \n" +
                    "Use coma (,) as separator for each arguments.\n";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());

        if (event.getArgs().split(",").length != 3)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Invalid given arguments.\n" +
                            "This command need 3 arguments: <playlist name>, <track title>, <url>.\n" +
                            "Use coma (,) as separator for each arguments.",
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
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    track.addTrackAsync(title, url);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        }
        trackId = track.getTrackId(url);

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), playlistName, this.table))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "`" + playlistName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (db.countPlaylistTrack(db.getPlaylistId(event.getAuthor().getIdLong(), playlistName, this.table), this.table) >= this.maxTrack)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Track for playlist has reached the maximum limit.",
                    true);
            event.reply(embed.build());
            return;
        }

        long finalTrackId = trackId;
        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addTrackToPlaylistAsync(event.getAuthor().getIdLong(), playlistName, finalTrackId, this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Track `" + title + "` added to `" + playlistName + "` playlist.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't add track `" + title + "` to `" + playlistName + "` playlist.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

package command.UserPlaylistCommand;

import YouTubeSearchApi.YoutubeClient;
import YouTubeSearchApi.entity.YoutubeVideo;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AddTrackToPlaylistCommand extends UserPlaylistBaseCommand
{
    private final YoutubeClient client;

    public AddTrackToPlaylistCommand(YoutubeClient ytc)
    {
        this.client = ytc;

        this.name = "add_track_to_my_playlist";
        this.aliases = new String[]{"attmp"};
        this.arguments = "<playlist name>, <url>";
        this.help = "Add a new track to user playlist. \n" +
                    "Use coma (,) as separator for each arguments.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        if (event.getArgs().split(",").length != 2)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Invalid given arguments.\n" +
                          "This command need 2 arguments: <playlist name>, <url>.\n" +
                          "Use coma (,) as separator for each arguments.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().split(",")[0].trim().replace("'", "\\'");
        String url = event.getArgs().split(",")[1].trim();

        UserPlaylistModel db = new UserPlaylistModel();

        if (db.isPlaylistNameExist(event.getAuthor().getIdLong(), playlistName) == false)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "`" + playlistName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        int userPlaylistTrackCount = db.countPlaylistTrack(
                db.getPlaylistId(event.getAuthor().getIdLong(), playlistName));

        if (userPlaylistTrackCount >= this.maxTrack)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Track for playlist has reached the maximum limit.",
                    true);
            event.reply(embed.build());
            return;
        }

        YoutubeVideo video;
        try
        {
            video = this.client.getInfoByVideoUrl(url);

            if (video == null)
            {
                embed.setTitle("Attention");
                embed.addField(
                        ":warning:",
                        "The given url is not valid.",
                        true);
                event.reply(embed.build());
                return;
            }
        }
        catch (Exception e)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "The given url is not valid.",
                    true);
            event.reply(embed.build());
            return;
        }

        long playlistId = db.getPlaylistId(event.getAuthor().getIdLong(), playlistName);
        video.setTitle(video.getTitle().replace("'", "\\'"));

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addTrackToPlaylist(playlistId, video.getUrl(), video.getTitle());

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Track `" + video.getTitle() + "` added to `" + playlistName + "` playlist.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't add track `" + video.getTitle() + "` to `" + playlistName + "` playlist.",
                        true);
            }
            event.reply(embed.build());
        });
    }
}

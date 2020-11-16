package command.playlist.guild;

import YouTubeSearchApi.YoutubeClient;
import YouTubeSearchApi.entity.YoutubeVideo;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildPlaylistModel;
import database.PremiumModel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AddTrackToPlaylistCommand extends GuildPlaylistBaseCommand
{
    private final YoutubeClient client;

    public AddTrackToPlaylistCommand(YoutubeClient ytc)
    {
        this.client = ytc;

        this.name = "add_track_to_guild_playlist";
        this.aliases = new String[]{"attgp"};
        this.arguments = "<playlist name>, <url>";
        this.help = "Add a new track to guild playlist. \n" +
                    "Use coma (,) as separator for each arguments.";
        this.cooldown = 2;
        this.guildOnly = true;
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        PremiumModel premium = new PremiumModel();

        if (!premium.isPremium(event.getGuild().getIdLong(), this.table))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "You are not premium, you can't use this command.",
                    true);
            event.reply(embed.build());
            return;
        }

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
        
        GuildPlaylistModel db = new GuildPlaylistModel();

        if (!db.isPlaylistNameExist(event.getGuild().getIdLong(), playlistName))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "`" + playlistName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        int guildPlaylistTrackCount = db.countPlaylistTrack(
                db.getPlaylistId(event.getGuild().getIdLong(), playlistName));
        GuildMusicManager manager = event.getClient().getSettingsFor(event.getGuild());

        if (guildPlaylistTrackCount >= manager.getMaxPlaylistTrackCount())
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Track for playlist has reached maximum limit.",
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

        long playlistId = db.getPlaylistId(event.getGuild().getIdLong(), playlistName);
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

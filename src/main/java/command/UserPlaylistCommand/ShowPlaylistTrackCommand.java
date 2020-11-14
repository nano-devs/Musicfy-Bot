package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.util.ArrayList;

public class ShowPlaylistTrackCommand extends UserPlaylistBaseCommand
{
    public ShowPlaylistTrackCommand()
    {
        this.name = "my_playlists_track";
        this.aliases = new String[]{"mpt", "playlists_track"};
        this.arguments = "<playlist name>";
        this.help = "Show all track(s) in the playlist owned by the user.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String playlistName = event.getArgs().trim().replace("'", "\\'");

        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        if (playlistName.length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please provide the name of the playlist.",
                    true);
            event.reply(embed.build());
            return;
        }

        UserPlaylistModel db = new UserPlaylistModel();

        if (db.isPlaylistNameExist(event.getAuthor().getIdLong(), playlistName) == false)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "`" + playlistName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getAuthor().getIdLong(), playlistName);

        if (tracks.size() <= 0)
        {
            embed.setTitle("Empty");
            embed.addField(
                    ":x:",
                    "No track inside playlist.",
                    true);
        }
        else
        {
            embed.setTitle("`" + playlistName + "` playlist :notes:");
            StringBuilder sb = new StringBuilder();
            sb.append("You have " + tracks.size() + " track(s) in the playlist.\n");

            for (int i = 0; i < tracks.size(); i++)
            {
                sb.append((i + 1) + ". ");
                sb.append("**" + tracks.get(i).title + "**");
                sb.append("\n");
            }
            embed.setDescription(sb.toString());
        }
        event.reply(embed.build());
    }
}

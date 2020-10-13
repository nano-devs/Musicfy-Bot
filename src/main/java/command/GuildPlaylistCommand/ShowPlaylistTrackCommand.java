package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;

public class ShowPlaylistTrackCommand extends GuildPlaylistBaseCommand
{
    public ShowPlaylistTrackCommand()
    {
        this.name = "show_guild_playlist_track";
        this.aliases = new String[]{"sgpt"};
        this.arguments = "<playlist name>";
        this.help = "Show all track from specific guild playlist.";
        this.cooldown = 2;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String playlistName = event.getArgs().trim();

        EmbedBuilder embed = new EmbedBuilder();

        if (playlistName.length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give playlist name.",
                    true);
            event.reply(embed.build());
            return;
        }

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getGuild().getIdLong(), playlistName, this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist with name `" + playlistName + "` is not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getGuild().getIdLong(), playlistName, this.table);

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
            embed.setTitle("Playlist `" + playlistName + "`");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < tracks.size(); i++)
            {
                sb.append((i + 1) + ". ");
                sb.append("**" + tracks.get(i).title + "**");
                sb.append(" `ID :" + tracks.get(i).trackId + "`");
                sb.append("\n");
            }
            embed.setDescription(sb.toString());
        }
        event.reply(embed.build());
    }
}

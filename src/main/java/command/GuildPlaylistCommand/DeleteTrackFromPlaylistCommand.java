package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;

public class DeleteTrackFromPlaylistCommand extends GuildPlaylistBaseCommand
{
    public DeleteTrackFromPlaylistCommand()
    {
        this.name = "delete_track_from_guild_playlist";
        this.aliases = new String[]{"dtfgp"};
        this.arguments = "<playlist name>, <track index>";
        this.help = "Delete existing track from guild playlist. \n" +
                    "Use coma (,) as separator for each arguments.";
        this.cooldown = 2;
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getArgs().split(",").length != 2)
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
        int index = Integer.parseInt(event.getArgs().split(",")[1].trim());

        PlaylistModel db = new PlaylistModel();
        if (db.countPlaylistTrack(db.getPlaylistId(event.getGuild().getIdLong(), playlistName, this.table), this.table) <= 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist is empty, no track to delete.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (db.deleteTrackFromPlaylist(event.getGuild().getIdLong(), playlistName, index, this.table))
        {
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Track deleted from playlist `" + playlistName + "`",
                    true);
        }
        else
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Can't delete track from playlist `" + playlistName + "`.",
                    true);
        }
        event.reply(embed.build());
    }
}
package command.playlist.guild;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

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
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        PremiumModel premium = new PremiumModel();

        if (premium.isPremium(event.getGuild().getIdLong(), this.table) == false)
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
                          "This command need 2 arguments: <playlist name>, <track index>.\n" +
                          "Use coma (,) as separator for each arguments.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().split(",")[0].trim().replace("'", "\\'");
        int index = Integer.parseInt(event.getArgs().split(",")[1].trim());

        PlaylistModel db = new PlaylistModel();
        int playlistSize = db.countPlaylistTrack(db.getPlaylistId(event.getGuild().getIdLong(), playlistName, this.table), this.table);

        if (playlistSize <= 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "`" + playlistName + "` playlist is empty, no track to delete.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (index > (playlistSize))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "`" + playlistName + "` playlist only have " + playlistSize + " track(s).",
                    true);
            event.reply(embed.build());
            return;
        }

        long playlistId = db.getPlaylistId(event.getAuthor().getIdLong(), playlistName, this.table);
        long playlistTrackId = db.getPlaylistTrackId(playlistId, index, this.table);

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.deleteTrackFromPlaylistAsync(playlistTrackId, this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Track deleted from `" + playlistName + "` playlist.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't delete track from `" + playlistName + "` playlist.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}
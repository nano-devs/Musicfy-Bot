package command.GuildPlaylistCommand;

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
                    "Use coma (,) as separator for each arguments.\n";
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

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.deleteTrackFromPlaylistAsync(event.getGuild().getIdLong(), playlistName, index, this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Track deleted from playlist `" + playlistName + "`",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't delete track from playlist `" + playlistName + "`.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.util.concurrent.CompletableFuture;

public class DeleteTrackFromPlaylistCommand extends UserPlaylistBaseCommand
{
    public DeleteTrackFromPlaylistCommand()
    {
        this.name = "delete_track_from_user_playlist";
        this.aliases = new String[]{"dtfup"};
        this.arguments = "<playlist name>, <track index>";
        this.help = "Delete existing track from user playlist. \n" +
                    "Use coma (,) as separator for each arguments.\n";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CompletableFuture.runAsync(() ->
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(event.getMember().getColor());
            PremiumModel premium = new PremiumModel();

            if (premium.isPremium(event.getAuthor().getIdLong(), this.table) == false)
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
            if (db.countPlaylistTrack(db.getPlaylistId(event.getAuthor().getIdLong(), playlistName, this.table), this.table) <= 0)
            {
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Playlist is empty, no track to delete.",
                        true);
                event.reply(embed.build());
                return;
            }

            if (db.deleteTrackFromPlaylist(event.getAuthor().getIdLong(), playlistName, index, this.table))
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
        });
    }
}

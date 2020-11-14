package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DeletePlaylistCommand extends UserPlaylistBaseCommand
{
    public DeletePlaylistCommand()
    {
        this.name = "delete_my_playlist";
        this.aliases = new String[]{"dmp"};
        this.arguments = "<playlist name>";
        this.help = "Delete the existing user playlist.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give playlist name to delete.",
                    true);
            event.reply(embed.build());
            return;
        }

        PlaylistModel db = new PlaylistModel();
        if (!db.isPlaylistNameExist(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist `" + event.getArgs().trim() + "` does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.deletePlaylistAndAllTrackFromPlaylistAsync(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table);
                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Playlist `" + event.getArgs().trim() + "` deleted.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "`" + event.getArgs().trim() + "` playlist not deleted.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserPlaylistModel;
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
                    "Please provide the name of the playlist you want to delete.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");
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

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.deletePlaylist(event.getAuthor().getIdLong(), playlistName);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist deleted.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "`" + playlistName + "` playlist not deleted.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

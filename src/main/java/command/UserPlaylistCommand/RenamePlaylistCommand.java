package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class RenamePlaylistCommand extends UserPlaylistBaseCommand
{
    public RenamePlaylistCommand()
    {
        this.name = "rename_my_playlist";
        this.aliases = new String[]{"rmp"};
        this.arguments = "<old playlist name> , <new playlist name>";
        this.help = "Rename user playlist. \n" +
                    "Use coma (,) as separator for old and new playlist name.";
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
                            "This command need 2 arguments: <old playlist name> , <new playlist name>.\n" +
                            "Use coma (,) as separator for each arguments.",
                    true);
            event.reply(embed.build());
            return;
        }

        String oldName = event.getArgs().split(",")[0].trim();
        String newName = event.getArgs().split(",")[1].trim();

        if (oldName.equals(newName))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "New name for playlist can not be same as the old name.",
                    true);
            event.reply(embed.build());
            return;
        }

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), oldName, this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "`" + oldName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.renamePlaylistAsync(event.getAuthor().getIdLong(), oldName, newName, this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Playlist renamed from `" + oldName + "` to `" + newName + "`.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't rename playlist.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

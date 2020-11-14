package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CreatePlaylistCommand extends UserPlaylistBaseCommand
{
    public CreatePlaylistCommand()
    {
        this.name = "create_new_playlist";
        this.aliases = new String[]{"cnp"};
        this.arguments = "<playlist name>";
        this.help = "Create a new user playlist. \n" +
                    "Playlist name can't be the same with your other playlist name.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        if (event.getArgs().trim().length() == 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please specify a name for the playlist.",
                    true);
            event.reply(embed.build());
            return;
        }

        UserPlaylistModel db = new UserPlaylistModel();

        if (db.countPlaylist(event.getAuthor().getIdLong()) >= this.maxPlaylist)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You have reached the maximum limit for playlist allocated to each user.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");

        if (db.isPlaylistNameExist(event.getAuthor().getIdLong(), playlistName))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You already have playlist with the same name.",
                    true);
            event.reply(embed.build());
            return;
        }

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.createPlaylist(event.getAuthor().getIdLong(), playlistName);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist is created.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "There's already playlist with name `" + playlistName + "`.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

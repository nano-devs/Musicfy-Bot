package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CreatePlaylistCommand extends UserPlaylistBaseCommand
{
    private final int maxPlaylist = 3;

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

        PlaylistModel db = new PlaylistModel();

        if (db.countPlaylist(event.getAuthor().getIdLong(), this.table) >= this.maxPlaylist)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You have reached the maximum limit for playlist allocated to each user.",
                    true);
            event.reply(embed.build());
            return;
        }

        String name = event.getArgs().trim().replace("'", "\\'");

        if (!db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), name, this.table))
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
                db.addPlaylistAsync(event.getAuthor().getIdLong(), name.trim(), this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + event.getArgs().trim() + "` playlist is created.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "There's already playlist with name `" + event.getArgs().trim() + "`.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

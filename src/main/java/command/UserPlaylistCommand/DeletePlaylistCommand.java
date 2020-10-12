package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;

public class DeletePlaylistCommand extends UserPlaylistBaseCommand
{
    public DeletePlaylistCommand()
    {
        this.name = "delete_user_playlist";
        this.aliases = new String[]{"dup"};
        this.arguments = "<playlist name>";
        this.help = "Delete existing user playlist.";
        this.cooldown = 2;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

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
        if (db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist `" + event.getArgs().trim() + "` not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (db.deletePlaylist(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Playlist `" + event.getArgs().trim() + "` deleted.",
                    true);
        }
        else
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Playlist `" + event.getArgs().trim() + "` not deleted.",
                    true);
        }
        event.reply(embed.build());
    }
}
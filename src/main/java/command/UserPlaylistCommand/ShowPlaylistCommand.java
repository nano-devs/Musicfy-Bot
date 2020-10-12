package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Playlist;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;

public class ShowPlaylistCommand extends UserPlaylistBaseCommand
{
    public ShowPlaylistCommand()
    {
        this.name = "show_user_playlist";
        this.aliases = new String[]{"sup"};
        this.help = "Show all existed user playlist.";
        this.cooldown = 2;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        PlaylistModel db = new PlaylistModel();
        ArrayList<Playlist> playlists = db.getAllPlaylist(event.getAuthor().getIdLong(), this.table);

        if (playlists == null || playlists.size() <= 0)
        {
            embed.setTitle("Empty");
            embed.addField(
                    ":x:",
                    "You have no playlist.",
                    true);
        }
        else
        {
            embed.setTitle("Your playlist :notes:");
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < playlists.size(); i++)
            {
                output.append((i + 1) + ". ");
                output.append("**" + playlists.get(i).name + "**");
                output.append(" `ID: " + playlists.get(i).id + "`");
                output.append("\n");
            }
            embed.setDescription(output.toString());
        }
        event.reply(embed.build());
    }
}
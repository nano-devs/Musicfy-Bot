package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Playlist;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.util.ArrayList;

public class ShowPlaylistCommand extends UserPlaylistBaseCommand
{
    public ShowPlaylistCommand()
    {
        this.name = "my_playlists";
        this.aliases = new String[]{"playlists", "mps", "playlist", "mp"};
        this.help = "Show all playlists owned by the user.";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();

        UserPlaylistModel db = new UserPlaylistModel();
        ArrayList<Playlist> playlists = db.getAllPlaylist(event.getAuthor().getIdLong());

        if (playlists == null || playlists.size() <= 0)
        {
            embed.setTitle("Empty");
            embed.addField(
                    ":x:",
                    "You don't have a playlist.",
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
                output.append(" `Track count: " + playlists.get(i).trackCount + "`");
                output.append("\n");
            }
            embed.setDescription(output.toString());
        }
        event.reply(embed.build());
    }
}

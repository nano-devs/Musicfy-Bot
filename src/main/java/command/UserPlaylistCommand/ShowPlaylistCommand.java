package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Playlist;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.util.ArrayList;

public class ShowPlaylistCommand extends UserPlaylistBaseCommand
{
    public ShowPlaylistCommand()
    {
        this.name = "show_user_playlist";
        this.aliases = new String[]{"sup"};
        this.help = "Show all playlists owned by the user.\n";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());

        PlaylistModel db = new PlaylistModel();
        ArrayList<Playlist> playlists = db.getAllPlaylist(event.getAuthor().getIdLong(), this.table);

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

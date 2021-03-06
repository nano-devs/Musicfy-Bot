package command.playlist.guild;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Playlist;
import database.GuildPlaylistModel;
import database.PremiumModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.util.ArrayList;

public class ShowPlaylistCommand extends GuildPlaylistBaseCommand
{
    public ShowPlaylistCommand()
    {
        this.name = "show_guild_playlist";
        this.aliases = new String[]{"sgp"};
        this.help = "Show all existed guild playlist.";
        this.cooldown = 2;
        this.guildOnly = true;
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        PremiumModel premium = new PremiumModel();

        if (!premium.isPremium(event.getGuild().getIdLong(), this.table))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "You are not premium, you can't use this command.",
                    true);
            event.reply(embed.build());
            return;
        }

        GuildPlaylistModel db = new GuildPlaylistModel();
        ArrayList<Playlist> playlists = db.getAllPlaylist(event.getGuild().getIdLong());

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
            embed.setTitle("Guild playlist :notes:");
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

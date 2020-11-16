package command.playlist.guild;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.GuildPlaylistModel;
import database.PremiumModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.util.ArrayList;;

public class ShowPlaylistTrackCommand extends GuildPlaylistBaseCommand
{
    public ShowPlaylistTrackCommand()
    {
        this.name = "show_guild_playlist_track";
        this.aliases = new String[]{"sgpt"};
        this.arguments = "<playlist name>";
        this.help = "Show all track from specific guild playlist.";
        this.cooldown = 2;
        this.guildOnly = true;
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String playlistName = event.getArgs().trim().replace("'", "\\'");

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

        if (playlistName.length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please provide the name of the playlist.",
                    true);
            event.reply(embed.build());
            return;
        }

        GuildPlaylistModel db = new GuildPlaylistModel();

        if (!db.isPlaylistNameExist(event.getGuild().getIdLong(), playlistName))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "`" + playlistName + "` playlist does not exist.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getGuild().getIdLong(), playlistName);

        if (tracks.size() <= 0)
        {
            embed.setTitle("Empty");
            embed.addField(
                    ":x:",
                    "No track inside playlist.",
                    true);
        }
        else
        {
            embed.setTitle("`" + playlistName + "` playlist :notes:");
            StringBuilder sb = new StringBuilder();
            sb.append("You have " + tracks.size() + " track(s) in the playlist.\n");

            for (int i = 0; i < tracks.size(); i++)
            {
                sb.append((i + 1) + ". ");
                sb.append("**" + tracks.get(i).title + "**");
                sb.append("\n");
            }
            embed.setDescription(sb.toString());
        }
        event.reply(embed.build());
    }
}

package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

public class RenamePlaylistCommand extends UserPlaylistBaseCommand
{
    public RenamePlaylistCommand()
    {
        this.name = "rename_user_playlist";
        this.aliases = new String[]{"rup"};
        this.arguments = "<old playlist name> , <new playlist name>";
        this.help = "Rename user playlist. \n" +
                    "Use coma (,) as separator for old and new playlist name.\n";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        PremiumModel premium = new PremiumModel();

        if (premium.isPremium(event.getAuthor().getIdLong(), this.table) == false)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "You are not premium, you can't use this command.",
                    true);
            event.reply(embed.build());
            return;
        }

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

        PlaylistModel db = new PlaylistModel();
        if (db.renamePlaylist(event.getAuthor().getIdLong(), oldName, newName, this.table))
        {
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Playlist renamed from `" + oldName + "` to `" + newName + "`.",
                    true);
        }
        else
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Can't rename playlist.",
                    true);
        }
        event.reply(embed.build());
    }
}

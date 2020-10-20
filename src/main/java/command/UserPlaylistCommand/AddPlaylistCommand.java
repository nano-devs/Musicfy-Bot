package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

public class AddPlaylistCommand extends UserPlaylistBaseCommand
{
    private final int maxPlaylist = 3;

    public AddPlaylistCommand()
    {
        this.name = "add_user_playlist";
        this.aliases = new String[]{"aup"};
        this.arguments = "<playlist name>";
        this.help = "Create a new user playlist. \n" +
                    "Playlist name can't be same with your existed other playlist.\n";
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

        if (event.getArgs().trim().length() == 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give a name to playlist.",
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
        if (db.addPlaylistAsync(event.getAuthor().getIdLong(), name.trim(), this.table))
        {
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Playlist `" + event.getArgs().trim() + "` created.",
                    true);
        }
        else
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's already playlist with name `" + event.getArgs().trim() + "`.",
                    true);
        }
        event.reply(embed.build());
    }
}

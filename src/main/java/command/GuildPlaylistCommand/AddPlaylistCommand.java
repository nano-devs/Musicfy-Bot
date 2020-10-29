package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AddPlaylistCommand extends GuildPlaylistBaseCommand
{
    private final int maxPlaylist = 3;

    public AddPlaylistCommand()
    {
        this.name = "add_guild_playlist";
        this.aliases = new String[]{"agp"};
        this.arguments = "<playlist name>";
        this.help = "Create a new guild playlist. \n" +
                    "The playlist name cannot be the same as the existing playlist in the guild.\n";
        this.cooldown = 2;
        this.guildOnly = true;
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        PremiumModel premium = new PremiumModel();

        if (premium.isPremium(event.getGuild().getIdLong(), this.table) == false)
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

        if (db.countPlaylist(event.getGuild().getIdLong(), this.table) >= this.maxPlaylist)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You have reached the maximum limit for playlist allocated to each guild.",
                    true);
            event.reply(embed.build());
            return;
        }

        String name = event.getArgs().trim().replace("'", "\\'");

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addPlaylistAsync(event.getGuild().getIdLong(), name.trim(), this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "Playlist `" + event.getArgs().trim() + "` created.",
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

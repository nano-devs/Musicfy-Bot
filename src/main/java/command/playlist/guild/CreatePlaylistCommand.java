package command.playlist.guild;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildPlaylistModel;
import database.PremiumModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CreatePlaylistCommand extends GuildPlaylistBaseCommand
{
    public CreatePlaylistCommand()
    {
        this.name = "create_guild_playlist";
        this.aliases = new String[]{"agp", "add_guild_playlist", "cgp"};
        this.arguments = "<playlist name>";
        this.help = "Create a new guild playlist. \n" +
                    "The playlist name cannot be the same as the other guild's playlist names.";
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

        GuildPlaylistModel db = new GuildPlaylistModel();

        if (db.countPlaylist(event.getGuild().getIdLong()) >= this.maxPlaylist)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You have reached the maximum limit for playlist allocated to each guild.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");

        if (!db.isPlaylistNameExist(event.getGuild().getIdLong(), playlistName))
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
                db.createPlaylist(event.getGuild().getIdLong(), playlistName);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist is created.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "There's already playlist with name `" + playlistName + "`.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

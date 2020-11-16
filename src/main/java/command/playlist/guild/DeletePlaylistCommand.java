package command.playlist.guild;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildPlaylistModel;
import database.PremiumModel;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DeletePlaylistCommand extends GuildPlaylistBaseCommand
{
    public DeletePlaylistCommand()
    {
        this.name = "delete_guild_playlist";
        this.aliases = new String[]{"dgp"};
        this.arguments = "<playlist name>";
        this.help = "Delete existing guild playlist.";
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

        if (premium.isPremium(event.getGuild().getIdLong(), this.table))
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "You are not premium, you can't use this command.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please provide the name of the playlist you want to delete.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");
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

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.deletePlaylist(event.getGuild().getIdLong(), playlistName);
                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist deleted.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "`" + playlistName + "` playlist not deleted.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

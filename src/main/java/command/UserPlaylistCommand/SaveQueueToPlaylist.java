package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SaveQueueToPlaylist extends UserPlaylistBaseCommand
{
    public SaveQueueToPlaylist()
    {
        this.name = "save_queue_to_playlist";
        this.arguments = "<playlist name>";
        this.aliases = new String[] { "sqtp" };
        this.help = "Add all tracks in queue to a new user playlist.\n";
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please specify a name for the playlist.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (musicManager.scheduler.getQueue().size() == 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "No tracks in queue.",
                    true);
            event.reply(embed.build());
            return;
        }

        UserPlaylistModel db = new UserPlaylistModel();

        if (db.countPlaylist(event.getAuthor().getIdLong()) >= this.maxPlaylist)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You have reached the maximum limit for playlist allocated to each user.",
                    true);
            event.reply(embed.build());
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");

        if (db.isPlaylistNameExist(event.getAuthor().getIdLong(), playlistName))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You already have playlist with the same name.",
                    true);
            event.reply(embed.build());
            return;
        }

        int loop = Math.min(musicManager.scheduler.getQueue().size(), this.maxTrack);
        int index = 0;
        String[] url = new String[loop];
        String[] title = new String[loop];

        for (AudioTrack track : musicManager.scheduler.getQueue())
        {
            if (index >= loop)
            {
                break;
            }
            url[index]= track.getInfo().uri;
            title[index] = track.getInfo().title;
            index++;
        }

        try
        {
            db.createPlaylist(event.getAuthor().getIdLong(), playlistName);
        }
        catch (SQLException e)
        {
            e.printStackTrace();

            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's already playlist with name `" + playlistName + "`.",
                    true);
            event.reply(embed.build());
            return;
        }

        long playlistId = db.getPlaylistId(event.getAuthor().getIdLong(), playlistName);

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addTrackToPlaylist(playlistId, url, title);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist is created " +
                              "and " + url.length + " track(s) from the queue have been added to the playlist.",
                        true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();

                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "`" + playlistName + "` playlist is created " +
                              "but unable to add tracks from queue.",
                        true);
            }

            event.reply(embed.build());
        });
    }
}

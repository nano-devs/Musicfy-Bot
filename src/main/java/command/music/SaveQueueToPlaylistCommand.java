package command.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import command.playlist.user.UserPlaylistBaseCommand;
import database.UserPlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SaveQueueToPlaylistCommand extends UserPlaylistBaseCommand
{
    public SaveQueueToPlaylistCommand()
    {
        this.name = "savequeue";
        this.arguments = "<playlist name>";
        this.aliases = new String[] { "save_queue_to_playlist", "sqtp" };
        this.help = "Add all tracks in queue to a new user playlist.\n";
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("Music");
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
                    "Queue is empty.",
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
            long playlistId = db.getPlaylistId(event.getAuthor().getIdLong(), playlistName);
            int playlistTrackCount = db.countPlaylistTrack(playlistId);
            int addLimit = musicManager.getMaxPlaylistTrackCount() - playlistTrackCount;

            if (addLimit <= 0)
            {
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Cannot save to playlist `" + playlistName + "`, playlist is full.",
                        true);
                return;
            }

            CompletableFuture.runAsync(() ->
            {
                try
                {
                    db.addTrackToPlaylist(playlistId, musicManager.scheduler.getQueue(), addLimit);

                    embed.setTitle("Success");
                    embed.addField(
                            ":white_check_mark:",
                            "`" + playlistName + "` playlist is created " +
                                    "and " + musicManager.scheduler.getQueue().size() +
                                    " track(s) from the queue have been added to the playlist.",
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
            return;
        }

        // if not exists yet, create new and add.
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
                    "Playlist `" + playlistName + "` already exists.",
                    true);
            event.reply(embed.build());
            return;
        }

        long playlistId = db.getPlaylistId(event.getAuthor().getIdLong(), playlistName);

        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addTrackToPlaylist(playlistId, musicManager.scheduler.getQueue(),
                        musicManager.getMaxPlaylistTrackCount());

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist is created " +
                              "and " + musicManager.scheduler.getQueue().size() +
                              " track(s) from the queue have been added to the playlist.",
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

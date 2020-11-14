package command.UserPlaylistCommand;

import YouTubeSearchApi.YoutubeClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.PlaylistModel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.sql.SQLException;
import java.util.ArrayList;
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

        PlaylistModel playlistModel = new PlaylistModel();

        if (playlistModel.countPlaylist(event.getAuthor().getIdLong(), this.table) >= this.maxPlaylist)
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

        if (!playlistModel.isPlaylistNameAvailable(event.getAuthor().getIdLong(), playlistName, this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "You already have playlist with the same name.",
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

        try
        {
            playlistModel.createPlaylist(event.getAuthor().getIdLong(), playlistName, this.table);
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

        int loop = Math.min(musicManager.scheduler.getQueue().size(), this.maxTrack);
        AudioTrack[] queue = (AudioTrack[]) musicManager.scheduler.getQueue().toArray();
        ArrayList<String> url = new ArrayList<>();
        ArrayList<String> title = new ArrayList<>();

        for (int i = 0; i < loop; i++)
        {
            url.add(queue[i].getInfo().uri);
            title.add(queue[i].getInfo().title);
        }

        try
        {
            playlistModel.createPlaylist(event.getAuthor().getIdLong(), playlistName, this.table);
        }
        catch (SQLException e)
        {
            e.printStackTrace();

            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's already playlist with name `" + playlistName + "`.",
                    true);
            return;
        }

        long playlitsId = playlistModel.getPlaylistId(event.getAuthor().getIdLong(), playlistName, this.table);

        CompletableFuture.runAsync(() ->
        {
            try
            {
                playlistModel.addTrackToPlaylist(playlitsId, (String[])url.toArray(), (String[])title.toArray(), this.table);

                embed.setTitle("Success");
                embed.addField(
                        ":white_check_mark:",
                        "`" + playlistName + "` playlist is created " +
                              "and " + this.maxTrack + " track(s) from the queue have been added to the playlist.",
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

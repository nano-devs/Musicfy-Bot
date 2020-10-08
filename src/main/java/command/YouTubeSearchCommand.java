package command;

import YouTubeSearchApi.YouTubeSearchClient;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import service.music.GuildMusicManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Search video from youtube based on keyword.
 */
public class YouTubeSearchCommand extends Command
{
    private final int maxVideoResult = 5;
    private final YouTubeSearchClient youtube;
    private final NanoClient nano;

    public YouTubeSearchCommand(NanoClient nano, YouTubeSearchClient youtube)
    {
        this.nano = nano;
        this.youtube = youtube;

        this.name = "search";
        this.aliases = new String[]{"yts", "yt s", "search", "s"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Search youtube video with specific keyword.";
        this.arguments = "<keyword>";
    }

    private boolean playVideo(String[] title, String[] url, int entry, CommandEvent event)
    {
        // check entry number
        if ((entry > this.maxVideoResult) ||
                (entry <= 0))
        {
            return false;
        }
        entry -= 1;

        // process premium access
        this.premiumMember(title[entry], url[entry], event);

        // get selected video detail
        GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setVolume(15);
        this.nano.loadAndPlayUrl(musicManager, event.getTextChannel(), url[entry], event.getAuthor());
        return true;
    }

    protected void premiumMember(String title, String url, CommandEvent event)
    {
        // insert track to database
        TrackModel trackModel = new TrackModel();
        trackModel.addTrack(title, url);
        long trackId = trackModel.getTrackId(url);

        // check premium membership
        if (trackModel.isPremium(event.getGuild().getIdLong(), "GUILD"))
        {
            GuildHistoryModel guild = new GuildHistoryModel();
            guild.addGuildHistory(event.getGuild().getIdLong(), trackId);
        }
        else if (trackModel.isPremium(event.getAuthor().getIdLong(), "USER"))
        {
            UserHistoryModel user = new UserHistoryModel();
            user.addUserHistory(event.getAuthor().getIdLong(), trackId);
        }
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String args = event.getArgs();
        // remove coma "," in args if exist
        if (args.contains(","))
        {
            args = args.replace(",", " ");
        }

        String response = this.youtube.Search(
                args,
                "snippet",
                "video",
                this.maxVideoResult);

        // create embed message
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.MAGENTA);
        embed.setTitle("Song selection | Reply the song number to continue");
        embed.setFooter("Song selection | Type the number to continue");
        embed.setDescription("");

        // process response
        // credit to https://www.geeksforgeeks.org/parse-json-java/

        // create json object to access data
        JSONObject obj = new JSONObject(response);

        // create json array to access video data in "items" scope
        JSONArray item = obj.getJSONArray("items");

        // create video list
        String[] url = new String[this.maxVideoResult];
        String[] title = new String[this.maxVideoResult];

        for (int i = 0; i < this.maxVideoResult; i++)
        {
            // get video id
            JSONObject id = item.getJSONObject(i).getJSONObject("id");
            String videoId = id.getString("videoId");

            // get video name/title
            JSONObject snippet = item.getJSONObject(i).getJSONObject("snippet");
            String videoTitle = snippet.getString("title");

            if (i == 0)
            {
                // get video thumbnail
                JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                String thumbnailUrl = thumbnails.getJSONObject("default").getString("url");
                embed.setThumbnail(thumbnailUrl);
            }

            // video response message
            String output = (i + 1) + ". [" +
                            videoTitle + "](" +
                            "https://www.youtube.com/watch?v=" + videoId + ")";

            url[i] = "https://www.youtube.com/watch?v=" + videoId;
            title[i] = videoTitle;

            // add video data to embed
            embed.appendDescription(output + "\n");
        }

        //event.reply(embed.build());
        AtomicReference<Message> msg = new AtomicReference<>();
        event.getChannel().sendMessage(embed.build()).queue((message) ->
                msg.set(message));

        // wait user response for playing video
        this.nano.getWaiter().waitForEvent(
                GuildMessageReceivedEvent.class,
                e -> e.getChannel().equals(event.getChannel())
                        && e.getAuthor().getId().equals(event.getAuthor().getId())
                        ,
                e -> {
                    int entry = Integer.parseInt(e.getMessage().getContentRaw());

                    if (!this.nano.getMusicService().joinUserVoiceChannel(event)) {
                        event.reply("not joined int voice channel");
                    }
                    else if (!this.playVideo(title, url, entry, event))
                    {
                        event.reply("Incorrect entry number.");
                    }
                },
                10, TimeUnit.SECONDS, () ->
//                        event.getChannel().deleteMessageById(msg.get().getId()).queue()
                        msg.get().delete().queue()
        );
    }
}

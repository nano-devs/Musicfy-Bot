package command;

import YouTubeSearchApi.YouTubeSearchClient;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import service.Music.GuildMusicManager;

import java.util.concurrent.TimeUnit;

/**
 * Search video from youtube based on keyword.
 */
public class YouTubeSearchCommand extends Command
{
    private final int _MaxVideoResult = 5;
    private final YouTubeSearchClient _Youtube;
    private final NanoClient _Nano;

    public YouTubeSearchCommand(NanoClient nano, YouTubeSearchClient youtube)
    {
        this._Nano = nano;
        this._Youtube = youtube;

        this.name = "youtube search";
        this.aliases = new String[]{"yts", "yt s"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Search youtube video with specific keyword.";
        this.arguments = "<keyword>";
    }

    private boolean PlayVideo(String video, int entry, CommandEvent event)
    {
        // check entry number
        if ((entry > this._MaxVideoResult) ||
                (entry <= 0))
        {
            return false;
        }

        // get selected video detail
        String url = video.split("\n")[entry];

        int first = url.indexOf("(");
        int last = url.indexOf(")");

        url = url.substring(first + 1, last);

        GuildMusicManager musicManager = this._Nano.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setVolume(15);
        this._Nano.loadAndPlayUrl(musicManager, event.getTextChannel(), url, event.getAuthor());
        return true;
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

        String response = this._Youtube.Search(
                args,
                "snippet",
                "video",
                this._MaxVideoResult);

        // create embed message
        EmbedBuilder embed = new EmbedBuilder();
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
        for (int i = 0; i < this._MaxVideoResult; i++)
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

            // add video data to embed
            embed.appendDescription(output + "\n");
        }
        event.reply(embed.build());

        // wait user response for playing video
        this._Nano.getWaiter().waitForEvent(
                GuildMessageReceivedEvent.class,
                e -> e.getChannel().equals(event.getChannel())
                        && e.getAuthor().getId().equals(event.getAuthor().getId())
                        ,
                e -> {
                    int entry = Integer.parseInt(e.getMessage().getContentRaw());

                    if (!this._Nano.getMusicService().joinUserVoiceChannel(event)) {

                    }
                    else if (!this.PlayVideo(embed.getDescriptionBuilder().toString(), entry, event))
                    {
                        event.reply("Incorrect entry number.");
                    }
                },
                10, TimeUnit.SECONDS, () ->
                        event.getMessage().delete()
                );
    }
}

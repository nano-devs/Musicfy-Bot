package command;

import YouTubeSearchApi.YouTubeSearchClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Search video from youtube based on keyword.
 */
public class YouTubeSearchCommand extends Command
{
    private final String _ApiKey;
    private final int _VideoNumber = 5;

    public YouTubeSearchCommand() throws IOException
    {
        this._ApiKey = Files.readAllLines(Paths.get("D:\\Project\\Java\\Nano.Jda\\api.txt")).get(4);

        this.name = "youtube search";
        this.aliases = new String[]{"yts", "yt s"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Search youtube video with specific keyword.";
        this.arguments = "<keyword>";
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

        YouTubeSearchClient client = new YouTubeSearchClient(this._ApiKey);
        String response = client.Search(
                args,
                "snippet",
                "video",
                this._VideoNumber);

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
        for (int i = 0; i < this._VideoNumber; i++)
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
    }
}

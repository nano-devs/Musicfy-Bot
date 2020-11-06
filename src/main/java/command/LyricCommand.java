package command;

import client.NanoClient;
import org.json.JSONObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LyricCommand extends Command
{
    private final NanoClient client;

    public LyricCommand(NanoClient client)
    {
        this.client = client;

        this.name = "lyric";
        this.arguments = "<song-title>";
        this.help = "Get lyric for music.\n" +
                    "If `song-title` is empty, the title of the currently playing music will be used.\n";
        this.guildOnly = true;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        GuildMusicManager musicManager = this.client.getGuildAudioPlayer(event.getGuild());
        String query = "";

        if (event.getArgs().trim().equals("") && musicManager.player.getPlayingTrack() == null)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Can't search lyric, because `song-title` is empty and currently no playing music.",
                    true);
            event.reply(embed.build());
            return;
        }
        else if (event.getArgs().trim().equals("") && musicManager.player.getPlayingTrack() != null)
        {
            query = musicManager.player.getPlayingTrack().getInfo().title;
        }
        else
        {
            query = event.getArgs();
        }

        String finalQuery = query;

        CompletableFuture.runAsync(() ->
        {
            try
            {
//                String output = Utils.httpRequest("https://lyrics.tsu.sh/v1/?q=" + URLEncoder.encode(finalQuery, StandardCharsets.UTF_8.toString()));
                URL url = new URL("https://lyrics.tsu.sh/v1/?q=" + URLEncoder.encode(finalQuery, StandardCharsets.UTF_8.toString()));
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                http.setRequestProperty("accept", "application/json");

                StringBuilder output = new StringBuilder();
                try (InputStreamReader stream = new InputStreamReader(http.getInputStream(), Charset.forName(StandardCharsets.UTF_8.name())))
                {
                    try (Reader reader = new BufferedReader(stream))
                    {
                        int c = 0;
                        while ((c = reader.read()) != -1)
                        {
                            output.append((char) c);
                        }
                    }
                }
                JSONObject json = new JSONObject(output.toString());
                embed.setTitle(((JSONObject)json.get("song")).get("full_title").toString());
                embed.setDescription(json.get("content").toString());
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Something happen when to request lyric, please try again later.",
                        true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                embed.setTitle("Failed");
                embed.addField(
                        ":x:",
                        "Can't find lyric.",
                        true);
            }
            event.reply(embed.build());
        });
    }
}

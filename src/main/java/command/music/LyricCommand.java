package command.music;

import YouTubeSearchApi.utility.Utils;
import client.NanoClient;
import org.json.JSONObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.awt.*;
import java.net.URLEncoder;
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
        this.aliases = new String[]{"lyrics"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        GuildMusicManager musicManager = this.client.getGuildAudioPlayer(event.getGuild());
        String query = "";

        if (event.getArgs().trim().equals("") && musicManager.player.getPlayingTrack() == null)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "Can't search lyric, because argument for `song-title` is empty and currently not playing any song.",
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
                String output = Utils.httpRequest("https://lyrics.tsu.sh/v1/?q=" + URLEncoder.encode(finalQuery, StandardCharsets.UTF_8.toString()));
                JSONObject json = new JSONObject(output);
                embed.setTitle(((JSONObject)json.get("song")).get("full_title").toString());
                embed.setThumbnail(((JSONObject)json.get("song")).get("icon").toString());
                embed.setDescription(json.get("content").toString());
            }
            catch (Exception e)
            {
                embed.clear();
                embed.setTitle("Failed");
                embed.setColor(new Color(211, 0, 137));
                embed.addField(
                        ":x:",
                        "Can't find lyric.",
                        true);
            }
            event.reply(embed.build());
        });
    }
}

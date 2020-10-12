package command;

import YouTubeSearchApi.*;
import YouTubeSearchApi.entity.YoutubeVideo;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import service.music.GuildMusicManager;
import service.music.PremiumService;

import java.awt.*;
import java.io.IOException;
import java.util.List;
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

    private boolean playVideo(YoutubeVideo[] video, int entry, CommandEvent event)
    {
        // check entry number
        if ((entry > this.maxVideoResult) ||
                (entry <= 0))
        {
            return false;
        }
        entry -= 1;

        // process premium access
        PremiumService.addHistory(video[entry].getTitle(), video[entry].getUrl(), event);

        // get selected video detail
        GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());
        musicManager.player.setVolume(15);
        this.nano.loadAndPlayUrl(musicManager, event.getTextChannel(), video[entry].getUrl(), event.getAuthor());
        return true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String keywords = event.getArgs();
        // remove coma "," in args if exist
        if (keywords.contains(","))
        {
            keywords = keywords.replace(",", " ");
        }

        YoutubeClient youtubeClient = new YoutubeClient();
        List<YoutubeVideo> response = null;
        try
        {
            response = youtubeClient.search(keywords, this.maxVideoResult);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // create embed message
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.MAGENTA);
        embed.setTitle("Song selection | Reply the song number to continue");
        embed.setFooter("Song selection | Type the number to continue");
        embed.setDescription("");

        // create video list

        String[] url = new String[this.maxVideoResult];
        String[] title = new String[this.maxVideoResult];
        YoutubeVideo[] video = new YoutubeVideo[this.maxVideoResult];

        for (int i = 0; i < this.maxVideoResult; i++)
        {
            if (i == 0)
            {
                embed.setThumbnail(response.get(i).getThumbnailUrl());
            }

            video[i].setUrl("https://www.youtube.com/watch?v=" + response.get(i).getId());
            video[i].setTitle(response.get(i).getTitle());

            // video response message
            String output = (i + 1) + ". " +
                    "[" + video[i].getTitle() + "]" +
                    "(" + video[i].getUrl() + ")";

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
                e ->
                {
                    int entry = Integer.parseInt(e.getMessage().getContentRaw());

                    if (!this.nano.getMusicService().joinUserVoiceChannel(event))
                    {
                        event.reply("not joined int voice channel");
                    }
                    else if (!this.playVideo(video, entry, event))
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

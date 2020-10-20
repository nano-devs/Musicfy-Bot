package command;

import YouTubeSearchApi.*;
import YouTubeSearchApi.entity.YoutubeVideo;
import YouTubeSearchApi.exception.NoResultFoundException;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.PremiumService;

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
        this.aliases = new String[]{"yts", "s"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Search youtube video with specific keyword.\n";
        this.arguments = "<keyword>";
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
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
        List<YoutubeVideo> videos = null;
        try
        {
            videos = youtubeClient.search(keywords, this.maxVideoResult);
        }
        catch (IOException | NoResultFoundException e)
        {
            e.printStackTrace();
            EmbedBuilder temp = new EmbedBuilder();
            temp.setColor(event.getMember().getColor());
            temp.setTitle("Failed");
            temp.addField(
                    ":x:",
                    e.getMessage(),
                    true);
            event.reply(temp.build());
            return;
        }

        // create embed message
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        embed.setTitle("Song selection | Reply the song number to continue");
        embed.setFooter("Song selection | Type the number to continue");
        embed.setDescription("");

        for (int i = 0; i < this.maxVideoResult; i++)
        {
            YoutubeVideo video = videos.get(i);
            if (i == 0)
            {
                embed.setThumbnail(video.getThumbnailUrl());
            }

            // video response message
            String output = (i + 1) + ". " +
                    "[" + video.getTitle() + "]" +
                    "(" + video.getUrl() + ")";

            // add video data to embed
            embed.appendDescription(output + "\n");
        }

        //event.reply(embed.build());
        AtomicReference<Message> msg = new AtomicReference<>();
        event.getChannel().sendMessage(embed.build()).queue((message) ->
                msg.set(message));

        // wait user response for playing video
        List<YoutubeVideo> finalVideos = videos;
        this.nano.getWaiter().waitForEvent(
                GuildMessageReceivedEvent.class,
                e -> e.getChannel().equals(event.getChannel())
                        && e.getAuthor().getId().equals(event.getAuthor().getId())
                ,
                e ->
                {
                    if (!this.nano.getMusicService().joinUserVoiceChannel(event))
                    {
                        event.reply("Not joined int voice channel");
                    }

                    int entry = Integer.parseInt(e.getMessage().getContentRaw());

                    // check entry number
                    if ((entry > this.maxVideoResult) ||
                            (entry <= 0))
                    {
                        event.reply("Incorrect entry number.");
                    }
                    entry -= 1;

                    // process premium access
                    PremiumService.addHistory(finalVideos.get(entry).getTitle(), finalVideos.get(entry).getUrl(), event);

                    // get selected video detail
                    GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());
                    this.nano.loadAndPlayUrl(musicManager, event.getTextChannel(), finalVideos.get(entry).getUrl(), event.getAuthor());
                },
                10, TimeUnit.SECONDS, () ->
                        msg.get().delete().queue()
        );
    }
}

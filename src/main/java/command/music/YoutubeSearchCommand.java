package command.music;

import YouTubeSearchApi.*;
import YouTubeSearchApi.entity.YoutubeVideo;
import YouTubeSearchApi.exception.NoResultFoundException;
import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.CustomEmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import service.music.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Search video from youtube based on keyword.
 */
public class YoutubeSearchCommand extends Command
{
    private final int maxVideoResult = 5;
    YoutubeClient youtubeClient;
    private final NanoClient nano;

    public YoutubeSearchCommand(NanoClient nano, YoutubeClient youtubeClient)
    {
        this.nano = nano;
        this.youtubeClient = youtubeClient;

        this.name = "search";
        this.aliases = new String[]{"yts", "s"};
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Search youtube video with specific keyword & select to playback the audio.";
        this.arguments = "<keywords>";
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String keywords = event.getArgs();
        if (keywords.isEmpty()) {
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":x: | Invalid Arguments", "Example usage: "
                    + event.getClient().getPrefix() + this.name + " " + this.arguments, true);
            event.reply(embedBuilder.build());
            return;
        }

        // remove coma "," in args if exist
        if (keywords.contains(","))
        {
            keywords = keywords.replace(",", " ");
        }

        List<YoutubeVideo> videos = null;
        try
        {
            videos = youtubeClient.search(keywords, this.maxVideoResult);
        }
        catch (IOException | NoResultFoundException e)
        {
            e.printStackTrace();
            CustomEmbedBuilder temp = new CustomEmbedBuilder();
            temp.setTitle("Failed");
            temp.addField(
                    ":x:",
                    e.getMessage(),
                    true);
            event.reply(temp.build());
            return;
        }

        if (videos.size() < 1) {
            event.reply(":x: | No result found...");
            return;
        }

        // create embed message
        CustomEmbedBuilder embed = new CustomEmbedBuilder();
        embed.setTitle("Song selection | Reply the song number to continue");
        embed.setFooter("Song selection | Type the number to continue");
        embed.setDescription("");

        for (int i = 0; i < videos.size(); i++)
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

            if (!video.getDuration().equals(""))
                output += " [" + video.getDuration() + "]";

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
                        event.reply(":x: | You are not connected to any voice channel.");
                        return;
                    }

                    GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());

                    if (musicManager.isInDjMode()) {
                        if (!MusicUtils.hasDjRole(event.getMember())) {
                            event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                            return;
                        }
                    }

                    if (musicManager.isQueueFull()) {
                        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
                        embedBuilder.addField(":x: | Queue is full", "Maximum queue length is 60.", true);
                        event.reply(embedBuilder.build());
                        return;
                    }

                    int entry = Integer.parseInt(e.getMessage().getContentRaw());

                    // check entry number
                    if ((entry > this.maxVideoResult) ||
                            (entry <= 0))
                    {
                        event.reply(":x: | Incorrect entry number.");
                        return;
                    }
                    entry -= 1;

                    // get selected video detail
                    this.nano.loadAndPlayUrl(musicManager, event.getTextChannel(),
                            finalVideos.get(entry).getUrl(), event.getMember());
                },
                10, TimeUnit.SECONDS, () ->
                        msg.get().delete().queue()
        );
    }
}

import YouTubeSearchApi.YouTubeSearchClient;
import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import command.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import service.Music.MusicService;

import javax.security.auth.login.LoginException;

public class Main {

    public final static String PLAY_EMOJI  = "\u25B6"; // Play Button
    public final static String PAUSE_EMOJI = "\u23F8"; // Pause Button
    public final static String STOP_EMOJI  = "\u23F9"; // Stop Button

    public static void main(String[] args) {

        String botToken = System.getenv("SAN_TOKEN");
        String youTubeToken = System.getenv("YT_TOKEN");

        NanoClient nano = new NanoClient(new MusicService(), new EventWaiter());
        YouTubeSearchClient youTubeSearchClient = new YouTubeSearchClient(youTubeToken);

        // Configure CommandClient
        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix("..");
        commandClientBuilder.setEmojis("\uD83D\uDC4C", "\u26A0", "\u2717");
        commandClientBuilder.setHelpWord("help");
        commandClientBuilder.setOwnerId("213866895806300161"); // Mandatory
        commandClientBuilder.setActivity(Activity.listening("JOMAMA"));

        commandClientBuilder.addCommand(new JoinCommand(nano));
        commandClientBuilder.addCommand(new LeaveCommand(nano));
        commandClientBuilder.addCommand(new PlayUrlCommand(nano));
        commandClientBuilder.addCommand(new VolumeCommand(nano));
        commandClientBuilder.addCommand(new SkipCommand(nano));
        commandClientBuilder.addCommand(new PauseCommand(nano));
        commandClientBuilder.addCommand(new ResumeCommand(nano));
        commandClientBuilder.addCommand(new YouTubeSearchCommand(nano, youTubeSearchClient));
        commandClientBuilder.addCommand(new NowPlayCommand(nano));

        CommandClient commandClient = commandClientBuilder.build();

        // JDA Builder
        JDABuilder builder = JDABuilder.createDefault(botToken);

        // Configure caching.
        configureMemoryUsage(builder);

        // Add JDA-Utilities command client.
        builder.addEventListeners(commandClient);
        builder.addEventListeners(nano.getWaiter());

        try {
            JDA jda = builder.build();
            nano.setJda(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static void configureMemoryUsage(JDABuilder builder) {
        // Disable cache for member activities (streaming/games/spotify)
        builder.disableCache(CacheFlag.ACTIVITY);

        // Only cache members who are either in a voice channel or owner of the guild
        builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));

        // Disable member chunking on startup
        builder.setChunkingFilter(ChunkingFilter.NONE);

        // Disable presence updates and typing events
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);

        // Consider guilds with more than 50 members as "large".
        // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
        builder.setLargeThreshold(50);
    }

}

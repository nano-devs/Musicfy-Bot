import YouTubeSearchApi.*;
import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import command.general.CustomPrefixCommand;
import command.general.HelpCommand;
import command.general.InviteCommand;
import command.general.VoteCommand;
import command.history.UserHistoryCommand;
import command.music.*;
import command.owner.*;
import listener.GuildEventListener;
import listener.MemberVoiceListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.discordbots.api.client.DiscordBotListAPI;
import service.music.MusicService;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {

    public final static String PLAY_EMOJI  = "\u25B6"; // Play Button
    public final static String PAUSE_EMOJI = "\u23F8"; // Pause Button
    public final static String STOP_EMOJI  = "\u23F9"; // Stop Button

    public static void main(String[] args) {
        // BASE
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int coreThreadPoolSize =  availableProcessors - 1 > 0 ? availableProcessors - 1 : 1;
        String botToken = System.getenv("SAN_TOKEN");
        String ytToken = System.getenv("DEVELOPER_KEY");
        String dblToken = System.getenv("DBL_TOKEN_2");
        String prefix = "m$";

        // Initialize Dependencies
        ScheduledExecutorService exec = new ScheduledThreadPoolExecutor(coreThreadPoolSize);
        NanoClient nano = new NanoClient(new MusicService(), new EventWaiter(exec, true));
        YoutubeClient youtubeClient = new YoutubeClient();
        DiscordBotListAPI dblApi = new DiscordBotListAPI.Builder()
                .token(dblToken)
                .botId("473023109666963467")
                .build();

        // Configure CommandClient
        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix(prefix);
        commandClientBuilder.setAlternativePrefix("@mention");
        commandClientBuilder.setEmojis("\uD83D\uDC4C", "\u26A0", "\u2717");
        commandClientBuilder.setOwnerId("213866895806300161"); // Mandatory
        commandClientBuilder.setCoOwnerIds("456130311365984267");
        commandClientBuilder.setActivity(Activity.listening("m$help"));
        commandClientBuilder.useHelpBuilder(false);
        commandClientBuilder.setGuildSettingsManager(nano);
        commandClientBuilder.setScheduleExecutor(exec);

        // Add Command & Inject Dependencies.
        // Free Commands
        commandClientBuilder.addCommand(new ApplyBassFilterCommand());
        commandClientBuilder.addCommand(new ApplyPianoFilterCommand());
        commandClientBuilder.addCommand(new ApplyFlatFilterCommand());
        commandClientBuilder.addCommand(new ClearFilterCommand());

        commandClientBuilder.addCommand(new NotifyMaintenanceCommand());
        commandClientBuilder.addCommand(new ShutdownCommand());
        commandClientBuilder.addCommand(new VoteCommand(nano));
        commandClientBuilder.addCommand(new InviteCommand());
        commandClientBuilder.addCommand(new CustomPrefixCommand());
        commandClientBuilder.addCommand(new DjModeCommand(nano));
        commandClientBuilder.addCommand(new JoinCommand(nano));
        commandClientBuilder.addCommand(new LeaveCommand(nano));
        commandClientBuilder.addCommand(new PlayUrlCommand(nano));
        commandClientBuilder.addCommand(new PlayCommand(nano));
        commandClientBuilder.addCommand(new VolumeCommand(nano));
        commandClientBuilder.addCommand(new SkipCommand(nano));
        commandClientBuilder.addCommand(new PauseCommand(nano));
        commandClientBuilder.addCommand(new ResumeCommand(nano));
        commandClientBuilder.addCommand(new YoutubeSearchCommand(nano, youtubeClient));
        commandClientBuilder.addCommand(new NowPlayCommand(nano));
        commandClientBuilder.addCommand(new RepeatCommand(nano));
        commandClientBuilder.addCommand(new ShowPaginatedQueueCommand(nano));
        commandClientBuilder.addCommand(new ShowGuildStateCommand(nano));
        commandClientBuilder.addCommand(new ShuffleCommand(nano));
        commandClientBuilder.addCommand(new RecommendationCommand(nano, youtubeClient));
        commandClientBuilder.addCommand(new LyricCommand(nano));
        commandClientBuilder.addCommand(new SaveQueueToPlaylistCommand());
        commandClientBuilder.addCommand(new PremiumUserCommand());
        commandClientBuilder.addCommand(new UserHistoryCommand());
        commandClientBuilder.addCommand(new command.playlist.user.CreatePlaylistCommand());
        commandClientBuilder.addCommand(new command.playlist.user.RenamePlaylistCommand());
        commandClientBuilder.addCommand(new command.playlist.user.DeletePlaylistCommand());
        commandClientBuilder.addCommand(new command.playlist.user.ShowPlaylistCommand());
        commandClientBuilder.addCommand(new command.playlist.user.AddTrackToPlaylistCommand(youtubeClient));
        commandClientBuilder.addCommand(new command.playlist.user.DeleteTrackFromPlaylistCommand());
        commandClientBuilder.addCommand(new command.playlist.user.ShowPlaylistTrackCommand());
        commandClientBuilder.addCommand(new command.playlist.user.PlayPlaylistCommand(nano));

        // Premium Guild Commands
//        commandClientBuilder.addCommand(new PremiumGuildCommand());
//        commandClientBuilder.addCommand(new GuildHistoryCommand(nano));
//        commandClientBuilder.addCommand(new command.playlist.guild.CreatePlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.RenamePlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.DeletePlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.ShowPlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.AddTrackToPlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.DeleteTrackFromPlaylistCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.ShowPlaylistTrackCommand());
//        commandClientBuilder.addCommand(new command.playlist.guild.PlayPlaylistCommand(nano));

        // Owner's commands
        commandClientBuilder.addCommand(new ChangePresenceCommand());

        CommandClient commandClient = commandClientBuilder.build();

        // Help command
        commandClient.addCommand(new HelpCommand(commandClient));

        // JDA Builder
        JDABuilder builder = JDABuilder.createDefault(botToken);

        // Configure caching.
        configureMemoryUsage(builder);

        // Add JDA-Utilities command client.
        builder.addEventListeners(commandClient);
        builder.addEventListeners(nano.getWaiter());
        builder.addEventListeners(new MemberVoiceListener(nano, exec));
        builder.addEventListeners(new GuildEventListener(dblApi, commandClient));

        try {
            JDA jda = builder.build();
            nano.setJda(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private static void configureMemoryUsage(JDABuilder builder) {
        // Disable cache for member activities (streaming/games/spotify)
        builder.disableCache(CacheFlag.ACTIVITY);
        builder.disableCache(CacheFlag.CLIENT_STATUS);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES);
        builder.disableCache(CacheFlag.EMOTE);

        // Only cache members who are either in a voice channel or owner of the guild
        builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));

        // Disable member chunking on startup
        builder.setChunkingFilter(ChunkingFilter.NONE);

        // Disable presence updates and typing events and more Guild Events
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_EMOJIS);

        builder.disableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS);

        // Consider guilds with more than 50 members as "large".
        // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
        builder.setLargeThreshold(50);
    }
}

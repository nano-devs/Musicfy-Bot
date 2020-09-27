package listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.WidgetUtil;
import org.jetbrains.annotations.NotNull;
import service.Music.GuildMusicManager;
import service.Music.MusicService;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MusicMessageListener extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private MusicService musicService;

    public MusicMessageListener(MusicService musicService) {
        this.musicService = musicService;

        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String contentRaw = event.getMessage().getContentRaw();

        if (!contentRaw.startsWith(".")){
            return;
        }

        String[] command = contentRaw.split(" ", 2);

        if (".play".equals(command[0]) && command.length == 2) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

            musicService.joinUserVoiceChannel(event);
            musicService.loadAndPlay(playerManager, musicManager, event.getChannel(), command[1]);
        } else if (".skip".equals(command[0])) {
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            musicService.skipTrack(musicManager, event.getChannel());
        } else if (".join".equals(command[0])) {
            VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
            System.out.println(event.getMember().getGuild().getName() + event.getMember().getUser().getName());
            if (voiceChannel == null) {
                event.getChannel().sendMessage("Are you sure you're in voice channel ?").queue();
                return;
            }
            musicService.joinVoiceChannel(event.getGuild(), voiceChannel);
            event.getChannel().sendMessage("Connected").queue();
        } else if (".leave".equals(command[0])) {
            musicService.leaveVoiceChannel(event.getGuild());
            event.getChannel().sendMessage("Leave Voice Channel").queue();
        }

        super.onGuildMessageReceived(event);
    }
}

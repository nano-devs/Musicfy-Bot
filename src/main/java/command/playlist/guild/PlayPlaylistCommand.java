package command.playlist.guild;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

import java.util.ArrayList;

public class PlayPlaylistCommand extends GuildPlaylistBaseCommand
{
    private final NanoClient nano;

    public PlayPlaylistCommand(NanoClient nano)
    {
        this.nano = nano;

        this.name = "play_guild_playlist";
        this.aliases = new String[]{"pgp"};
        this.arguments = "<playlist name>";
        this.help = "Play all track from specific guild playlist.";
        this.cooldown = 2;
        this.guildOnly = true;
        this.category = new Category("Guild Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        VoiceChannel userVoiceChannel = event.getMember().getVoiceState().getChannel();
        if (userVoiceChannel == null)
        {
            event.reply(":x: | You are not connected to any voice channel");
            return;
        }

        if (event.getSelfMember().getVoiceState().getChannel() == null)
        {
            event.getGuild().getAudioManager().openAudioConnection(userVoiceChannel);
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(event.getMember().getColor());
        PremiumModel premium = new PremiumModel();

        if (premium.isPremium(event.getGuild().getIdLong(), this.table) == false)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "You are not premium, you can't use this command.",
                    true);
            event.reply(embed.build());
            return;
        }

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give playlist name you want to to play.",
                    true);
            return;
        }

        String playlistName = event.getArgs().trim().replace("'", "\\'");
        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getGuild().getIdLong(), playlistName, this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's no playlist with name `" + playlistName + "`.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getGuild().getIdLong(), playlistName, this.table);

        if (tracks.size() <= 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's no track in playlist `" + playlistName + "`.",
                    true);
            event.reply(embed.build());
        }
        else
        {
            GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());
            if (musicManager.isInDjMode()) {
                if (!MusicUtils.hasDjRole(event.getMember())) {
                    event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                    return;
                }
            }

            int addedSize = 0;
            for (int i = 0; i < tracks.size(); i++)
            {
                this.nano.loadAndPlayUrl(musicManager, null, tracks.get(i).url, event.getMember());
                addedSize += 1;
                if (musicManager.isQueueFull()) {
                    break;
                }
            }
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Add " + addedSize + " track(s) to the queue.",
                    true);
            embed.setFooter("Only song with duration less than 1 hour added to queue");
            event.reply(embed.build());
        }
    }
}

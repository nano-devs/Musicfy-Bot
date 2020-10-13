package command.GuildPlaylistCommand;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.GuildMusicManager;

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
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getArgs().trim().length() <= 0)
        {
            embed.setTitle("Attention");
            embed.addField(
                    ":warning:",
                    "Please give playlist name you want to to play.",
                    true);
            return;
        }

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getGuild().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's no playlist with name `" + event.getArgs().trim() + "`.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getGuild().getIdLong(), event.getArgs().trim(), this.table);

        if (tracks.size() <= 0)
        {
            embed.setTitle("Failed");
            embed.addField(
                ":x:",
                "There's no track in playlist `" + event.getArgs().trim() + "`.",
                true);
            event.reply(embed.build());
        }
        else
        {
            GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());

            for (int i = 0; i < tracks.size(); i++)
            {
                this.nano.loadAndPlayUrl(musicManager, event.getTextChannel(), tracks.get(i).url, event.getAuthor());
            }
        }
    }
}

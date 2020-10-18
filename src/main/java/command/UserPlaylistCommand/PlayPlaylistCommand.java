package command.UserPlaylistCommand;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.Entity.Track;
import database.PlaylistModel;
import database.PremiumModel;
import net.dv8tion.jda.api.EmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.PremiumService;

import java.util.ArrayList;

public class PlayPlaylistCommand extends UserPlaylistBaseCommand
{
    private final NanoClient nano;

    public PlayPlaylistCommand(NanoClient nano)
    {
        this.nano = nano;

        this.name = "play_user_playlist";
        this.aliases = new String[]{"pup"};
        this.arguments = "<playlist name>";
        this.help = "Play all track from specific user playlist.\n";
        this.cooldown = 2;
        this.category = new Category("User Playlist");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        PremiumModel premium = new PremiumModel();

        if (premium.isPremium(event.getAuthor().getIdLong(), this.table) == false)
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

        PlaylistModel db = new PlaylistModel();

        if (db.isPlaylistNameAvailable(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table))
        {
            embed.setTitle("Failed");
            embed.addField(
                    ":x:",
                    "There's no playlist with name `" + event.getArgs().trim() + "`.",
                    true);
            event.reply(embed.build());
            return;
        }

        ArrayList<Track> tracks = db.getTrackListFromPlaylist(event.getAuthor().getIdLong(), event.getArgs().trim(), this.table);

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
            embed.setTitle("Success");
            embed.addField(
                    ":white_check_mark:",
                    "Add " + tracks.size() + " track to queue.",
                    true);

            GuildMusicManager musicManager = this.nano.getGuildAudioPlayer(event.getGuild());

            for (int i = 0; i < tracks.size(); i++)
            {
                PremiumService.addHistory(tracks.get(i).title, tracks.get(i).url, event);
                this.nano.loadAndPlayUrl(musicManager, null, tracks.get(i).url, event.getMember());
            }
            event.reply(embed.build());
        }
    }
}

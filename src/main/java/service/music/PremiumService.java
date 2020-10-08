package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.GuildHistoryModel;
import database.TrackModel;
import database.UserHistoryModel;

public class PremiumService
{
    public PremiumService()
    {

    }

    /**
     * Add played music to history database
     * @param title track/song/video title
     * @param url track/song/video url
     * @param event
     */
    public static void addHistory(String title, String url, CommandEvent event)
    {
        // insert track to database
        TrackModel trackModel = new TrackModel();
        trackModel.addTrack(title, url);
        long trackId = trackModel.getTrackId(url);

        if (trackModel.isPremium(event.getGuild().getIdLong(), "GUILD"))
        {
            GuildHistoryModel guild = new GuildHistoryModel();
            guild.addGuildHistory(event.getGuild().getIdLong(), trackId);
        }

        if (trackModel.isPremium(event.getAuthor().getIdLong(), "USER"))
        {
            UserHistoryModel user = new UserHistoryModel();
            user.addUserHistory(event.getAuthor().getIdLong(), trackId);
        }
    }
}

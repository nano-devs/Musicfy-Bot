package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import database.*;
import io.donatebot.api.DBClient;
import io.donatebot.api.Donation;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PremiumService
{
    private String apiKey;
    private String serverID;

    private String premiumUser = "li4NBOCVxk";

    public PremiumService()
    {

    }

    public PremiumService(String serverID, String apiKey)
    {
        this.apiKey = apiKey;
        this.serverID = serverID;
    }

    /**
     * Add played music to history database
     * @param title track/song/video title
     * @param url track/song/video url
     * @param event command even listener
     */
    public static void addHistory(String title, String url, CommandEvent event)
    {
        TrackModel trackModel = new TrackModel();
        try
        {
            trackModel.addTrackAsync(title, url);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
//        CompletableFuture.runAsync(() ->
//        {
//
//        });

        long trackId = trackModel.getTrackId(url);

        PremiumModel db = new PremiumModel();

        if (db.isPremium(event.getGuild().getIdLong(), "GUILD"))
        {
            GuildHistoryModel guild = new GuildHistoryModel();

            CompletableFuture.runAsync(() ->
            {
                try
                {
                    guild.addGuildHistoryAsync(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), trackId);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        }

        if (db.isPremium(event.getAuthor().getIdLong(), "USER"))
        {
            UserHistoryModel user = new UserHistoryModel();

            CompletableFuture.runAsync(() ->
            {
                try
                {
                    user.addUserHistoryAsync(event.getAuthor().getIdLong(), trackId);
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Check donation API
     */
    public void check()
    {
        DBClient dbClient = new DBClient(this.serverID, this.apiKey);
//        String[] statuses = {"Completed", "Reversed", "Refunded"};
        String[] statuses = {"Completed"};
        CompletableFuture<Donation[]> future = dbClient.getNewDonations(statuses);
        PremiumModel db = new PremiumModel();

        // step
        // check product subscription
        // - if new user/guild, add to database
        // - if existing user/guild, renew subscription date
        // (maybe) delete user/guild who not renew subscription over 1 month
        // mark all successful transaction as "done"

        CompletableFuture.runAsync(() ->
        {
            try
            {
                for (Donation item : future.get())
                {
                    boolean result = false;

                    // user
                    if (item.getSellerCustoms().get("Your In-Game ID").equals(this.premiumUser))
                    {
                        continue;
                    }
                    else if (item.getProductID().equals(this.premiumUser))
                    {
                        long userId = Long.parseLong(item.getBuyerID());
                        if (db.isNew(userId, "USER"))
                        {
                            result = db.addPremiumAsync(userId, "USER");
                        }
                        else
                        {
                            result = db.renewSubscriptionAsync(userId, "USER");
                        }
                    }
                    // guild
                    else
                    {
                        long guildId = Long.parseLong(item.getBuyerID());
                        if (db.isNew(guildId, "GUILD"))
                        {
                            result = db.addPremiumAsync(guildId, "GUILD");
                        }
                        else
                        {
                            result = db.renewSubscriptionAsync(guildId, "GUILD");
                        }
                    }

                    // mark as done
                    if (result)
                    {
                        dbClient.markDonation(item.getTransactionID(), true, true);
                    }
                }
                System.out.println("Done");
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        });
    }
}



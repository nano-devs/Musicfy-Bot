package database;

import client.NanoClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class GuildHistoryModel extends BaseModel
{
    public GuildHistoryModel()
    {
        super();
    }

    /**
     * Add played track by guild member to database
     * @param guidId Guild id that the user reside in.
     * @param trackId Track id that the user play
     * @return
     */
    public boolean addGuildHistory(long guidId, long userId, long trackId)
    {
        String query = "INSERT INTO GUILD_HISTORY (GUILD_ID, USER_ID, TRACK_ID) " +
                "VALUES (" + guidId + ", " + userId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }


    /**
     * Get history of user
     * @param guildId user id
     * @param client JDA object
     * @return
     */
    public String GetGuildHistory(long guildId, JDA client)
    {
        String query =
                "SELECT guild_history.USER_ID, track.TITLE, track.URL, guild_history.DATE " +
                "FROM guild_history left join track on guild_history.TRACK_ID = track.ID " +
                "WHERE GUILD_ID = " + guildId +
                " GROUP BY guild_history.DATE " +
                " ORDER by guild_history.DATE DESC";

        try (
                Connection connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password)
        )
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    String history = "";
                    String date = "";
                    int counter = 1;
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm");

                    while (result.next())
                    {
                        if (!date.equals(result.getDate(4).toString()))
                        {
                            date = result.getDate(4).toString();
                            history += date + "\n";
                            counter = 1;
                        }

                        StringBuilder output = new StringBuilder();
                        output.append(counter + ". ");
                        output.append("`" + format.format(result.getTime(4)) + "` ");
                        output.append(client.getUserById(result.getLong(1)).getName() + "");
//                        output.append((result.getLong(1)));
                        output.append(" [" + result.getString(2) + "]");
                        output.append("(" + result.getString(3) +")\n");

                        if (history.length() + output.length() < 2048)
                        {
                            history += output;
                        }
                        else
                        {
                            break;
                        }
                    }
                    return history;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
}

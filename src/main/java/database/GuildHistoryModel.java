package database;

import net.dv8tion.jda.api.JDA;

import java.sql.*;
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
     * @param userId User id
     * @param url Track url.
     * @param title Track title.
     * @return
     */
    public boolean addGuildHistory(long guidId, long userId, String url, String title) throws SQLException
    {
        String query =
                "INSERT INTO GUILD_HISTORY (GUILD_ID, USER_ID, URL, TITLE) " +
                "VALUES (" + guidId + ", " + userId + ", ';'" + url + "';', '" + title + "')";
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
                "SELECT USER_ID, TITLE, URL, DATE " +
                "FROM GUILD_HISTORY " +
                "WHERE GUILD_ID = " + guildId +
                " GROUP BY DATE " +
                "ORDER by DATE DESC";

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
//                        output.append(client.getUserById(result.getLong(1)).getName() + "");
//                        output.append((result.getLong(1)));
                        output.append(" [" + result.getString(2) + "]");
                        output.append("(" + result.getString(3) +")\n");
                        counter++;

                        if (history.length() + output.length() < 1900)
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
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return "";
    }
}

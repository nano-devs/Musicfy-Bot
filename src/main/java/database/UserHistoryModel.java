package database;

import java.sql.*;
import java.text.SimpleDateFormat;

public class UserHistoryModel extends BaseModel
{
    public UserHistoryModel()
    {
        super();
    }

    /**
     * Add played track by user to history database
     * @param userId User id that issued the command
     * @param url Track url.
     * @param title Track title.
     * @return true if success to insert data to database.
     */
    public boolean addUserHistory(long userId, String url, String title) throws SQLException
    {
        String query =
                "INSERT INTO USER_HISTORY (USER_ID, URL, TITLE) " +
                "VALUES (" + userId + ", '" + url + "', '" + title.replace("'", "\\'") + "')";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Get history of user
     * @param userId user id
     * @return
     */
    public String getUserHistory(long userId)
    {
        String query =
                "SELECT TITLE, URL, DATE " +
                "FROM USER_HISTORY " +
                "WHERE USER_ID = " + userId +
                " GROUP BY DATE " +
                "ORDER by DATE DESC ";

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
                        if (!date.equals(result.getDate(3).toString()))
                        {
                            date = result.getDate(3).toString();
                            history += date + "\n";
                            counter = 1;
                        }

                        StringBuilder output = new StringBuilder();
                        output.append(counter + ". ");
                        output.append("`" + format.format(result.getTime(3)) + "` ");
                        output.append("[" + result.getString(1) + "]");
                        output.append("(" + result.getString(2) +")\n");
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

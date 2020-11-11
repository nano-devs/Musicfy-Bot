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
     * @param trackId Track id that the user play
     * @return true if success to insert data to database.
     */
    public boolean addUserHistoryAsync(long userId, long trackId) throws SQLException
    {
        String query =
                "INSERT INTO USER_HISTORY (USER_ID, TRACK_ID) " +
                "VALUES (" + userId + ", " + trackId + ")";
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
                "SELECT track.TITLE, track.URL, user_history.DATE " +
                "FROM user_history left join track on user_history.TRACK_ID = track.ID " +
                "WHERE USER_ID = " + userId +
                " GROUP BY user_history.DATE " +
                " ORDER by DATE DESC ";

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

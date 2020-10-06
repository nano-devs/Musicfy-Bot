package database;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.sql.*;
import java.util.Calendar;

/**
 * MYSQL database connection
 */
public class MYSQL
{
    private Connection connection;
    private final String url;
    private final String database;
    private final String username;
    private final String password;

    public MYSQL()
    {
        this.database = "nano";
        this.username = "root";
        this.password = "";
        this.url = "jdbc:mysql://localhost:3306/" + database;
        this.connection = null;
        this.connect();
    }

    public MYSQL(String database, String username, String password)
    {
        this.database = database;
        this.username = username;
        this.password = password;
        this.url = "jdbc:mysql://localhost:3306/" + this.database;
        this.connection = null;
        this.connect();
    }

    /**
     * Connect to database.
     */
    public void connect()
    {
        try
        {
            if (this.connection == null)
            {
                this.connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password);
            }
            else if (this.connection.isClosed())
            {
                this.connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get database connection state
     * @return <code>Connection</code>
     */
    public Connection getConnection()
    {
        return this.connection;
    }

    /**
     * Execute Select query and return
     * @param query SQL query
     * @return the result of Select query as ResultSet
     */
    public ResultSet executeSelectQuery(String query)
    {
        ResultSet resultSets = null;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement(query);
            return statement.executeQuery();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return resultSets;
    }

    /**
     * Execute Insert/Update/Delete query
     * @param query SQL query
     * @return number of changed record
     */
    public int executeUpdateQuery(String query)
    {
        try
        {
            PreparedStatement statement = this.connection.prepareStatement(query);
            return statement.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addPremiumUser(long id)
    {
        DateTime start = new DateTime(Calendar.getInstance().getTime());
        DateTime end = start.plus(Months.ONE);

        String startSql = "STR_TO_DATE('" + start.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";
        String endSql = "STR_TO_DATE('" + end.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";


        String query = "INSERT INTO PREMIUM_USER " +
                        "VALUES (" + id + ", " + startSql + ", " + endSql + ")";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Check user id/guild id is premium or not
     * @param id USER ID or GUILD ID
     * @param type USER od GUILD
     * @return true if the id is registered.
     */
    public boolean isPremium(long id, String type)
    {
        String query = "SELECT COUNT(" + type + "_ID), DATE_END " +
                        "FROM PREMIUM_" + type +
                        "WHERE " + type + "_ID = " + id;
        ResultSet result = this.executeSelectQuery(query);

        try
        {
            if (result.getInt(0) > 0)
            {
                String date = result.getDate("END_DATE").toString();
                if (DateTime.parse(date).isBefore(DateTime.now()))
                {
                    return true;
                }
                return false;
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }




    public boolean addUserHistory(long userId, long trackId)
    {
        String query = "INSERT INTO USER_HISTORY " +
                        "VALUES (" + userId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }

    public boolean addGuildHistory(long guidId, long trackId)
    {
        String query = "INSERT INTO GUILD_HISTORY " +
                "VALUES (" + guidId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Add new track/song to database
     * @param title Track/Song title
     * @param url Link to the track/song
     * @return True if track added to database, False otherwise.
     */
    public boolean addTrack(String title, String url)
    {
        String query = "INSERT INTO TRACK (TITLE, URL) " +
                        "VALUES ('" + title + "', '" + url + "')";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Get track id based on track url.
     * beacuse title can be duplicate but url not.
     * @param url track url
     * @return Track id. 0 if no track/song find based on url.
     */
    public long getTrackId(String url)
    {
        String query = "SELECT ID " +
                        "FROM TRACK " +
                        "WHERE URL = " + url;
        ResultSet result = this.executeSelectQuery(query);

        try
        {
            return result.getInt(0);
        }
        catch (Exception e)
        {
            return 0;
        }

    }
}

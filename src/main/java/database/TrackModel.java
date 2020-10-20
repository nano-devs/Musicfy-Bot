package database;

import database.Entity.Track;

import java.sql.*;

public class TrackModel extends BaseModel
{
    public TrackModel()
    {
        super();
    }

    /**
     * Add new track/song to database
     * @param title Track/Song title
     * @param url Link to the track/song
     * @return True if track added to database, False otherwise.
     */
    public boolean addTrackAsync(String title, String url)
    {
        title = title.replace("'", "\\'");
        String query = 
                "INSERT INTO TRACK (TITLE, URL) " +
                "VALUES ('" + title + "', '" + url + "')";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Get track id based on track url.
     * because title can be duplicate but url not.
     * @param url track url
     * @return Track id. 0 if no track/song find based on url.
     */
    public long getTrackId(String url)
    {
        String query =
                "SELECT ID " +
                "FROM TRACK " +
                "WHERE URL = '" + url + "'";

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
                    if (result.next())
                    {
                        long trackId = result.getLong(1);
                        return trackId;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get track object by url.
     * @param url registered url in database.
     * @return
     */
    public Track getObject(String url)
    {
        String query =
                "SELECT ID, TITLE, URL " +
                "FROM TRACK " +
                "WHERE URL = '" + url + "'";

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
                    if (result.next())
                    {
                        Track track = new Track();
                        track.trackId = result.getLong(1);
                        track.title = result.getString(2);
                        track.url = result.getString(3);
                        return track;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get track object by url.
     * @param trackId registered id in database.
     * @return
     */
    public Track getObject(long trackId)
    {
        String query =
                "SELECT ID, TITLE, URL " +
                "FROM TRACK " +
                "WHERE ID = " + trackId;

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
                    if (result.next())
                    {
                        Track track = new Track();
                        track.trackId = result.getLong(1);
                        track.title = result.getString(2);
                        track.url = result.getString(3);
                        return track;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

package database;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.Playlist;
import database.Entity.Track;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class UserPlaylistModel extends BaseModel
{
    protected final int maxTrackEachPlaylist;

    public UserPlaylistModel()
    {
        super();
        this.maxTrackEachPlaylist = 20;
    }

    /**
     * Check if the availability of playlist name.
     * @param userId user id
     * @param playlistName playlist name
     * @return true if playlist name is exist.
     */
    public boolean isPlaylistNameExist(long userId, String playlistName)
    {
        String query =
                "SELECT COUNT(NAME) " +
                "FROM USER_PLAYLIST " +
                "WHERE NAME = '" + playlistName + "' " +
                "AND USER_ID = " + userId;

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    result.next();
                    return result.getInt(1) == 1;
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * create new playlist
     * @param userId user id
     * @param playlistName playlist name
     * @return true if playlist successfully created.
     */
    public boolean createPlaylist(long userId, String playlistName) throws SQLException
    {
        String query =
                "INSERT INTO USER_PLAYLIST (USER_ID, NAME) " +
                "VALUES (" + userId + ", '" + playlistName + "')";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * get playlist id
     * @param userId user id
     * @param playlistName playlist name
     * @return playlist id
     */
    public long getPlaylistId(long userId, String playlistName)
    {
        String query =
                "SELECT ID " +
                "FROM USER_PLAYLIST " +
                "WHERE USER_ID = " + userId + " " +
                "AND NAME = '" + playlistName + "'";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    result.next();
                    return result.getLong(1);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * get all playlist from user playlist
     * @param userId user id
     * @return array of playlist
     */
    public ArrayList<Playlist> getAllPlaylist(long userId)
    {
        String query =
                "SELECT USER_PLAYLIST.NAME, COUNT(USER_PLAYLIST_TRACK.URL) " +
                "FROM USER_PLAYLIST " +
                "LEFT JOIN USER_PLAYLIST_TRACK ON USER_PLAYLIST.ID = USER_PLAYLIST_TRACK.USER_PLAYLIST_ID " +
                "WHERE USER_PLAYLIST.USER_ID = " + userId + " " +
                "GROUP BY USER_PLAYLIST.NAME";

        int countPlaylist = this.countPlaylist(userId);

        if (countPlaylist == 0)
        {
            return null;
        }

        ArrayList<Playlist> playlists = new ArrayList<>(countPlaylist);

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    while (result.next())
                    {
                        playlists.add(
                                new Playlist(
                                        0, 
                                        result.getString(1), 
                                        0, 
                                        result.getInt(2))
                        );
                    }
                    return playlists;
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
     * Count playlist for user
     * @param userId user id
     * @return number of user playlist
     */
    public int countPlaylist(long userId)
    {
        String query =
                "SELECT COUNT(ID) " +
                "FROM USER_PLAYLIST " +
                "WHERE USER_ID = " + userId;

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
                    result.next();
                    return result.getInt(1);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * rename playlist name
     * @param userId user id
     * @param oldName old playlist name
     * @param newName new playlist name
     * @return true if playlist renamed
     */
    public boolean renamePlaylist(long userId, String oldName, String newName) throws SQLException
    {
        String query =
                "UPDATE USER_PLAYLIST " +
                "SET NAME = '" + newName + "' " +
                "WHERE USER_ID = " + userId +
                " AND NAME = '" + oldName + "'";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * delete playlist and all track inside the playlist
     * @param userId user id
     * @param playlistName Playlist name.
     * @return true if playlist deleted.
     */
    public boolean deletePlaylist(long userId, String playlistName) throws SQLException
    {
        String query =
                "DELETE FROM USER_PLAYLIST \n" +
                "WHERE USER_PLAYLIST.NAME = '" + playlistName + "' " +
                "AND USER_PLAYLIST.USER_ID = " + userId + ";";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * add track to playlist
     * @param playlistId playlist id
     * @param url Track url.
     * @param title Track title.
     * @return true if track successfully added to playlist.
     */
    public boolean addTrackToPlaylist(long playlistId, String url, String title) throws SQLException
    {
        String query =
                "INSERT INTO USER_PLAYLIST_TRACK (USER_PLAYLIST_ID, URL, TITLE) VALUES " +
                "(" + playlistId + ", '" + url + "', '" + title + "')";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * add multiple track to playlist
     * @param playlistId playlist id
     * @param queue queue from music manager.
     * @param addLimit add limit.
     * @return true if track successfully added to playlist.
     */
    public boolean addTrackToPlaylist(long playlistId, BlockingQueue<AudioTrack> queue, int addLimit) throws SQLException
    {
        String query = "INSERT INTO USER_PLAYLIST_TRACK (USER_PLAYLIST_ID, URL, TITLE) VALUES \n";

        int counter = 0;
        for (AudioTrack track : queue)
        {
            query += "(" + playlistId + ", '" + track.getInfo().uri + "', '" + track.getInfo().title + "'),\n";

            counter += 1;
            if (counter >= addLimit)
                break;
        }

        // Removes the last `,` and add semicolon.
        query = query.substring(0, query.length() - 2) + ";";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * get all track from playlist
     * @param userId user id
     * @param playlistName playlist name
     * @return array of track
     */
    public ArrayList<Track> getTrackListFromPlaylist(long userId, String playlistName)
    {
        String query =
                "SELECT USER_PLAYLIST_TRACK.URL, USER_PLAYLIST_TRACK.TITLE \n" +
                "FROM  USER_PLAYLIST_TRACK \n" +
                "JOIN USER_PLAYLIST ON USER_PLAYLIST_TRACK.USER_PLAYLIST_ID = USER_PLAYLIST.ID \n" +
                "WHERE USER_PLAYLIST.USER_ID = " + userId + " AND USER_PLAYLIST.NAME = '" + playlistName + "'";

        ArrayList<Track> tracks = new ArrayList<>(this.maxTrackEachPlaylist);

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    while (result.next())
                    {
                        tracks.add(
                                new Track(0, 
                                result.getString(2), 
                                result.getString(1))
                        );
                    }
                    return tracks;
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
     * Count number of track in playlist.
     * @param playlistId playlist id
     * @return number of track in specific playlist
     */
    public int countPlaylistTrack(long playlistId)
    {
        String query =
                "SELECT COUNT(URL) " +
                "FROM USER_PLAYLIST_TRACK " +
                "WHERE USER_PLAYLIST_ID = " + playlistId;

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    result.next();
                    return result.getInt(1);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get id from table user playlist track
     * @param playlistId Playlist id
     * @param trackIndex track index
     * @return playlist track id
     */
    public long getPlaylistTrackId(long playlistId, int trackIndex)
    {
        String query =
                "SELECT ID " +
                "FROM USER_PLAYLIST_TRACK " +
                "WHERE USER_PLAYLIST_ID = " + playlistId + " ";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password))
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    for (int i = 0; i < trackIndex; i++)
                    {
                        result.next();
                        if ((trackIndex - 1) == i)
                        {
                            return result.getLong(1);
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * delete track from playlist
     * @param playlistTrackId user_playlist_track id
     * @return true if track from playlist deleted.
     */
    public boolean deleteTrackFromPlaylistAsync(long playlistTrackId) throws SQLException
    {
        String query =
                "DELETE FROM USER_PLAYLIST_TRACK " +
                "WHERE ID = " + playlistTrackId + " ";

        return this.executeUpdateQuery(query) > 0;
    }
}

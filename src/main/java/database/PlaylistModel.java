package database;

import database.Entity.Playlist;
import database.Entity.Track;

import java.sql.*;
import java.util.ArrayList;

public class PlaylistModel extends BaseModel
{
    protected final int maxPlaylist;
    protected final int maxTrackEachPlaylist;

    public PlaylistModel()
    {
        super();

        this.maxPlaylist = 3;
        this.maxTrackEachPlaylist = 20;
    }

    /**
     * Check if the playlist name is available or not.
     * @param id user id/ guild id
     * @param name playlist name
     * @param table "USER" or "GUILD"
     * @return
     */
    @Deprecated
    public boolean isPlaylistNameAvailable(long id, String name, String table)
    {
        String query =
                "SELECT COUNT(NAME) " +
                "FROM " + table + "_PLAYLIST " +
                "WHERE NAME = '" + name + "' " +
                "AND " + table + "_ID = " + id;

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
                    int counter = result.getInt(1);
                    return counter == 0;
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
     * @param id user id / guild id
     * @param name playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean createPlaylist(long id, String name, String table) throws SQLException
    {
        String query =
                "INSERT INTO " + table + "_PLAYLIST (" + table + "_ID, NAME) " +
                "VALUES (" + id + ", '" + name + "')";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * get playlist id
     * @param id user id / guild id
     * @param name playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public long getPlaylistId(long id, String name, String table)
    {
        String query =
                "SELECT ID " +
                "FROM " + table + "_PLAYLIST " +
                "WHERE " + table + "_ID = " + id + " " +
                "AND NAME = '" + name + "'";

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
     * get all playlist from user/guild
     * @param id user id/ guild id
     * @param table "USER" / "GUILD"
     * @return
     */
    public ArrayList<Playlist> getAllPlaylist(long id, String table)
    {
        String query =
                "SELECT " + table + "_PLAYLIST.NAME, COUNT(" + table + "_PLAYLIST_TRACK.URL) " +
                "FROM " + table + "_PLAYLIST " +
                "LEFT JOIN " + table + "_PLAYLIST_TRACK ON " + table + "_PLAYLIST.ID = " + table + "_PLAYLIST_TRACK." + table + "_PLAYLIST_ID " +
                "WHERE " + table + "_PLAYLIST." + table + "_ID = " + id + " " +
                "GROUP BY USER_PLAYLIST.NAME";

        int countPlaylist = this.countPlaylist(id, table);

        if (countPlaylist == 0)
        {
            return null;
        }

        ArrayList<Playlist> playlists = new ArrayList<Playlist>(countPlaylist);

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
                    while (result.next())
                    {
                        playlists.add(
                                new Playlist(
                                        0,
                                        result.getString(1),
                                        this.maxTrackEachPlaylist,
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
     * Count playlist number for user/guild
     * @param id user id/ guild id
     * @param table "USER" / "GUILD"
     * @return
     */
    public int countPlaylist(long id, String table)
    {
        String query =
                "SELECT COUNT(ID) " +
                "FROM " + table + "_PLAYLIST " +
                "WHERE " + table + "_ID = " + id;

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
     * @param id user id / guild id
     * @param oldName old playlist name
     * @param newName new playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean renamePlaylist(long id, String oldName, String newName, String table) throws SQLException
    {
        String query =
                "UPDATE " + table + "_PLAYLIST " +
                "SET NAME = '" + newName + "' " +
                "WHERE " + table + "_ID = " + id +
                " AND NAME = '" + oldName + "'";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * delete playlist and all track inside the playlist
     * @param id user/guild id
     * @param playlistName Playlist name.
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean deletePlaylistAndAllTrackFromPlaylistAsync(long id, String playlistName, String table) throws SQLException
    {
        String query =
                "DELETE " + table + "_PLAYLIST, " + table + "_PLAYLIST_TRACK \n" +
                "FROM " + table + "_PLAYLIST \n" +
                "LEFT JOIN " + table + "_PLAYLIST_TRACK ON " + table + "_PLAYLIST_TRACK." + table + "_PLAYLIST_ID = " + table + "_PLAYLIST.ID \n" +
                "WHERE " + table + "_PLAYLIST.NAME = '" + playlistName + "' " +
                "AND " + table + "_PLAYLIST." + table + "_ID = " + id + " ";

        return this.executeUpdateQuery(query) > 0;
    }





    /**
     * add track to playlist
     * @param playlistId playlist id
     * @param url Track url.
     * @param title Track title.
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean addTrackToPlaylist(long playlistId, String url, String title, String table) throws SQLException
    {
        String query =
                "INSERT INTO " + table + "_PLAYLIST_TRACK (" + table + "_PLAYLIST_ID, URL, TITLE) VALUES " +
                "(" + playlistId + ", '" + url + "', '" + title + "')";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * add multiple track to playlist
     * @param playlistId playlist id
     * @param url List of track url.
     * @param title List of track title.
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean addTrackToPlaylist(long playlistId, String[] url, String[] title, String table) throws SQLException
    {
        String query = "";
        for (int i = 0; i < url.length; i++)
        {
            query +=
                    "INSERT INTO " + table + "_PLAYLIST_TRACK (" + table + "_PLAYLIST_ID, URL, TITLE) VALUES " +
                    "(" + playlistId + ", '" + url[i] + "', '" + title[i] + "');\n";
        }
        System.out.println(query);
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * get all track from playlist
     * @param id user id / guild id
     * @param name playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public ArrayList<Track> getTrackListFromPlaylist(long id, String name, String table)
    {
        String query =
                "SELECT " + table + "_PLAYLIST_TRACK.URL, " + table + "_PLAYLIST_TRACK.TITLE \n" +
                "FROM  " + table + "_PLAYLIST_TRACK \n" +
                "JOIN " + table + "_PLAYLIST ON " + table + "_PLAYLIST_TRACK." + table + "_PLAYLIST_ID = " + table + "_PLAYLIST.ID \n" +
                "WHERE " + table + "_PLAYLIST." + table + "_ID = " + id + " AND " + table + "_PLAYLIST.NAME = '" + name + "'";

        ArrayList<Track> tracks = new ArrayList<Track>(this.maxTrackEachPlaylist);

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
     * @param table "USER" / "GUILD"
     * @return
     */
    public int countPlaylistTrack(long playlistId, String table)
    {
        String query =
                "SELECT COUNT(URL) " +
                "FROM " + table + "_PLAYLIST_TRACK " +
                "WHERE " + table + "_PLAYLIST_ID = " + playlistId;

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

    public ArrayList<Track> getTrackListFromPlaylist(long playlistId)
    {
        String query =
                "SELECT user_playlist.NAME, track.ID, track.TITLE, track.URL " +
                "FROM user_playlist " +
                "LEFT JOIN user_playlist_track ON user_playlist.ID = user_playlist_track.USER_PLAYLIST_ID " +
                "LEFT JOIN track ON user_playlist_track.TRACK_ID = track.ID " +
                "WHERE user_playlist.ID = " + playlistId;

        ArrayList<Track> tracks = new ArrayList<>(this.maxTrackEachPlaylist);

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
                    while (result.next())
                    {
                        tracks.add(
                                new Track(result.getInt(2),
                                        result.getString(3),
                                        result.getString(4))
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
     * Get id from table guild/user playlist track
     * @param playlistId Playlist id
     * @param trackIndex track index
     * @param table "USER" / "GUILD"
     * @return
     */
    public long getPlaylistTrackId(long playlistId, int trackIndex, String table)
    {
        String query =
                "SELECT ID " +
                "FROM " + table + "_PLAYLIST_TRACK " +
                "WHERE " + table + "_PLAYLIST_ID = " + playlistId + " ";

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
     * @param playlistTrackId user/guild_playlist_track id
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean deleteTrackFromPlaylistAsync(long playlistTrackId, String table) throws SQLException
    {
        String query =
                "DELETE FROM " + table + "_PLAYLIST_TRACK " +
                "WHERE ID = " + playlistTrackId + " ";

        return this.executeUpdateQuery(query) > 0;
    }
}

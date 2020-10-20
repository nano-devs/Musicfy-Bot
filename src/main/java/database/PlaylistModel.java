package database;

import database.Entity.Playlist;
import database.Entity.Track;

import java.sql.*;
import java.util.ArrayList;

public class PlaylistModel extends BaseModel
{
    private final int maxPlaylist;
    private final int maxTrackEachPlaylist;

    public PlaylistModel()
    {
        super();

        this.maxPlaylist = 3;
        this.maxTrackEachPlaylist = 20;
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
                    int counter = result.getInt(1);
                    return counter;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return -1;
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
                "SELECT COUNT(TRACK_ID) " +
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
                    int counter = result.getInt(1);
                    return counter;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return -1;
    }

    /**
     * Check if the playlist name is available or not.
     * @param id user id/ guild id
     * @param name playlist name
     * @param table "USER" or "GUILD"
     * @return
     */
    public boolean isPlaylistNameAvailable(long id, String name, String table)
    {
        String query =
                "SELECT COUNT(NAME) " +
                "FROM " + table + "_PLAYLIST " +
                "WHERE NAME = '" + name + "'";

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
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return false;
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
                    long counter = result.getLong(1);
                    return counter;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return -1;
    }

    /**
     * get track id from playlist
     * @param playlistId playlist id
     * @param name playlist name
     * @param trackIndex registered index on playlist
     * @param table "USER" or "GUILD"
     * @return
     */
    public long getTrackId(long playlistId, String name, int trackIndex, String table)
    {
        String query =
                "SELECT " + table + "_PLAYLIST_TRACK.TRACK_ID " +
                "FROM " + table + "_PLAYLIST " +
                "LEFT JOIN " + table + "_PLAYLIST_TRACK ON " + table + "_PLAYLIST.ID = " + table + "_PLAYLIST_TRACK." + table + "_PLAYLIST_ID " +
                "WHERE " + table + "_PLAYLIST.ID = " + playlistId ;
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
                    int counter = 0;
                    while (result.next())
                    {
                        if (counter == trackIndex - 1)
                        {
                            long trackId = result.getLong(1);
                            return trackId;
                        }
                    }
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return -1;
    }

    /**
     * create new playlist
     * @param id user id / guild id
     * @param name playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean addPlaylistAsync(long id, String name, String table) throws SQLException
    {
        String query =
                "INSERT INTO " + table + "_PLAYLIST (" + table + "_ID, NAME) " +
                "VALUES (" + id + ", '" + name + "')";

        if (!this.isPlaylistNameAvailable(id, name, table))
        {
            return false;
        }

        if (this.countPlaylist(id, table) < this.maxPlaylist)
        {
            return this.executeUpdateQuery(query) > 0;
        }
        return false;
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
                "SELECT ID, NAME " +
                "FROM " + table + "_PLAYLIST " +
                "WHERE " + table + "_ID = " + id;

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
                                new Playlist(result.getInt(1),
                                result.getString(2),
                                this.maxTrackEachPlaylist)
                        );
                    }
                    return playlists;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * get specific playlist
     * @param playlistId playlist id
     * @return
     */
    public Playlist getPlaylist(long playlistId)
    {
        String query =
                "SELECT ID, NAME " +
                "FROM USER_PLAYLIST " +
                "WHERE ID = " + playlistId;

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
                    return new Playlist(result.getInt(1),
                            result.getString(2),
                            this.maxTrackEachPlaylist);
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * rename playlist name
     * @param id user id / guild id
     * @param oldName old playlist name
     * @param newName new playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean renamePlaylistAsync(long id, String oldName, String newName, String table) throws SQLException
    {
        String query =
                "UPDATE " + table + "_PLAYLIST " +
                "SET NAME = '" + newName + "' " +
                "WHERE " + table + "_ID = " + id +
                " AND NAME = '" + oldName + "'";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * delete user/guild playlist
     * @param id user id / guild id
     * @param name playlist name
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean deletePlaylistAsync(long id, String name, String table) throws SQLException
    {
        String query =
                "DELETE FROM " + table + "_PLAYLIST " +
                "WHERE " + table + "_ID = " + id +
                " AND NAME = '" + name + "'";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * add track to playlist
     * @param playlistId playlist id
     * @param trackId track id
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean addTrackToPlaylistAsync(long playlistId, long trackId, String table) throws SQLException
    {
        String query =
                "INSERT INTO " + table + "_PLAYLIST_TRACK VALUES " +
                "(" + playlistId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * add track to playlist
     * @param id user id / guild id
     * @param name playlist name
     * @param trackId track id
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean addTrackToPlaylistAsync(long id, String name, long trackId, String table) throws SQLException
    {
        long playlistId = this.getPlaylistId(id, name, table);
        return this.addTrackToPlaylistAsync(playlistId, trackId, table);
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
                "SELECT " + table + "_playlist.ID, track.ID, track.TITLE, track.URL " +
                "FROM " + table + "_playlist " +
                "LEFT JOIN " + table + "_playlist_track ON " + table + "_playlist.ID = " + table + "_playlist_track." + table + "_PLAYLIST_ID " +
                "LEFT JOIN track ON " + table + "_playlist_track.TRACK_ID = track.ID " +
                "WHERE " + table + "_playlist." + table + "_ID = " + id +
                " AND " + table + "_playlist.NAME = '" + name + "'";

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
                                new Track(result.getInt(2),
                                        result.getString(3),
                                        result.getString(4))
                        );
                    }
                    return tracks;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }

    public ArrayList<Track> getTrackListFromPlaylist(long playlistId)
    {
        String query =
                "SELECT user_playlist.NAME, track.ID, track.TITLE, track.URL " +
                "FROM user_playlist " +
                "LEFT JOIN user_playlist_track ON user_playlist.ID = user_playlist_track.USER_PLAYLIST_ID " +
                "LEFT JOIN track ON user_playlist_track.TRACK_ID = track.ID " +
                "WHERE user_playlist.ID = " + playlistId;

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
                                new Track(result.getInt(2),
                                        result.getString(3),
                                        result.getString(4))
                        );
                    }
                    return tracks;
                }
            }
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * delete track from playlist
     * @param playlistId playlist id
     * @param trackId track id
     * @param table "USER" / "GUILD"
     * @return
     */
    public boolean deleteTrackFromPlaylistAsync(long playlistId, long trackId, String table) throws SQLException
    {
        String query =
                "DELETE FROM " +table + "_PLAYLIST_TRACK " +
                "WHERE " + table + "_PLAYLIST_ID = " + playlistId + " " +
                "AND TRACK_ID = " + trackId;
        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * delete track from playlist
     * @param id user id / guild id
     * @param name playlist name
     * @param table
     * @param trackIndex track index to delete based on registered index by ShowPlaylistTrackCommand
     * @return
     */
    public boolean deleteTrackFromPlaylistAsync(long id, String name, int trackIndex, String table) throws SQLException
    {
        long playlistId = this.getPlaylistId(id, name, table);
        long trackId = this.getTrackId(playlistId, name, trackIndex, table);
        if (trackIndex <= trackId)
        {
            return this.deleteTrackFromPlaylistAsync(playlistId, trackId, table);
        }
        return false;
    }
}

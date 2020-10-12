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

    public long getTrackId(long id, String name, int trackIndex, String table)
    {
        String query =
                "SELECT TRACK_ID " +
                "FROM " + table + "PLAYLIST " +
                "WHERE " + table + "_ID = " + id +
                " AND NAME = '" + name + "'";
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





    public boolean addPlaylist(long id, String name, String table)
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

    public boolean renamePlaylist(long userId, String oldName, String newName, String table)
    {
        String query =
                "UPDATE " + table + "_PLAYLIST " +
                "SET NAME = '" + newName + "' " +
                "WHERE " + table + "_ID = " + userId +
                " AND NAME = '" + oldName + "'";
        return this.executeUpdateQuery(query) > 0;
    }

    public boolean deletePlaylist(long userId, String name, String table)
    {
        String query =
                "DELETE FROM " + table + "_PLAYLIST " +
                "WHERE " + table + "_ID = " + userId +
                " AND NAME = '" + name + "'";
        return this.executeUpdateQuery(query) > 0;
    }

    public boolean addTrackToPlaylist(long playlistId, long trackId, String table)
    {
        String query =
                "INSERT INTO " + table + "_PLAYLIST_TRACK VALUES " +
                "(" + playlistId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }

    public boolean addTrackToPlaylist(long id, String name, long trackId, String table)
    {
        long playlistId = this.getPlaylistId(id, name, table);
        return this.addTrackToPlaylist(playlistId, trackId, table);
    }

    public ArrayList<Track> getTrackListFromPlaylist(long userId, String name, String table)
    {
        String query =
                "SELECT user_playlist.ID, track.ID, track.TITLE, track.URL " +
                "FROM user_playlist " +
                "LEFT JOIN user_playlist_track ON user_playlist.ID = user_playlist_track.USER_PLAYLIST_ID " +
                "LEFT JOIN track ON user_playlist_track.TRACK_ID = track.ID " +
                "WHERE user_playlist.USER_ID = " + userId +
                " AND user_playlist.NAME = '" + name + "'";

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

    public boolean deleteTrackFromPlaylist(long playlistId, long trackId, String table)
    {
        String query =
                "DELETE FROM " +table + "_PLAYLIST_TRACK " +
                "WHERE " + table + "_PLAYLIST_ID = " + playlistId + " " +
                "AND TRACK_ID = " + trackId;
        return this.executeUpdateQuery(query) > 0;
    }

    public boolean deleteTrackFromPlaylist(long id, String name, long trackIndex, String table)
    {
        long playlistId = this.getPlaylistId(id, name, table);
        long trackId = this.countPlaylistTrack(playlistId, table);
        if (trackIndex <= trackId)
        {
            return this.deleteTrackFromPlaylist(playlistId, trackId, table);
        }
        return false;
    }
}

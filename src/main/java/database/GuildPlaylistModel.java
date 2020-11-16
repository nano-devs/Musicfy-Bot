package database;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import database.Entity.Playlist;
import database.Entity.Track;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class GuildPlaylistModel extends BaseModel
{
    protected final int maxTrackEachPlaylist;

    public GuildPlaylistModel()
    {
        super();
        this.maxTrackEachPlaylist = 20;
    }

    /**
     * Check if the availability of playlist name.
     * @param guildId guild id
     * @param playlistName playlist name
     * @return true if playlist name is exist.
     */
    public boolean isPlaylistNameExist(long guildId, String playlistName)
    {
        String query =
                "SELECT COUNT(NAME) " +
                "FROM GUILD_PLAYLIST " +
                "WHERE NAME = '" + playlistName.replace("'", "\\'") + "' " +
                "AND GUILD_ID = " + guildId;

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
     * @param guildId guild id
     * @param playlistName playlist name
     * @return true if playlist successfully created.
     */
    public boolean createPlaylist(long guildId, String playlistName) throws SQLException
    {
        String query =
                "INSERT INTO GUILD_PLAYLIST (GUILD_ID, NAME) " +
                "VALUES (" + guildId + ", '" + playlistName.replace("'", "\\'") + "')";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * get playlist id
     * @param guildId guild id
     * @param playlistName playlist name
     * @return playlist id
     */
    public long getPlaylistId(long guildId, String playlistName)
    {
        String query =
                "SELECT ID " +
                "FROM GUILD_PLAYLIST " +
                "WHERE GUILD_ID = " + guildId + " " +
                "AND NAME = '" + playlistName.replace("'", "\\'") + "'";

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
     * get all playlist from guild playlist
     * @param guildId guild id
     * @return array of playlist
     */
    public ArrayList<Playlist> getAllPlaylist(long guildId)
    {
        String query =
                "SELECT GUILD_PLAYLIST.NAME, COUNT(GUILD_PLAYLIST_TRACK.URL) " +
                        "FROM GUILD_PLAYLIST " +
                        "LEFT JOIN GUILD_PLAYLIST_TRACK ON GUILD_PLAYLIST.ID = GUILD_PLAYLIST_TRACK.GUILD_PLAYLIST_ID " +
                        "WHERE GUILD_PLAYLIST.GUILD_ID = " + guildId + " " +
                        "GROUP BY GUILD_PLAYLIST.NAME";

        int countPlaylist = this.countPlaylist(guildId);

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
     * Count playlist for guild
     * @param guildId guild id
     * @return number of guild playlist
     */
    public int countPlaylist(long guildId)
    {
        String query =
                "SELECT COUNT(ID) " +
                "FROM GUILD_PLAYLIST " +
                "WHERE GUILD_ID = " + guildId;

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
     * rename playlist name
     * @param guildId guild id
     * @param oldName old playlist name
     * @param newName new playlist name
     * @return true if playlist renamed
     */
    public boolean renamePlaylist(long guildId, String oldName, String newName) throws SQLException
    {
        String query =
                "UPDATE GUILD_PLAYLIST " +
                "SET NAME = '" + newName + "' " +
                "WHERE GUILD_ID = " + guildId +
                " AND NAME = '" + oldName + "'";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * delete playlist and all track inside the playlist
     * @param guildId guild id
     * @param playlistName Playlist name.
     * @return true if playlist deleted.
     */
    public boolean deletePlaylist(long guildId, String playlistName) throws SQLException
    {
        String query =
                "DELETE FROM GUILD_PLAYLIST \n" +
                "WHERE GUILD_PLAYLIST.NAME = '" + playlistName + "' " +
                "AND GUILD_PLAYLIST.GUILD_ID = " + guildId + ";";

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
                "INSERT INTO GUILD_PLAYLIST_TRACK (GUILD_PLAYLIST_ID, URL, TITLE) VALUES " +
                "(" + playlistId + ", '" + url + "', '" + title.replace("'", "\\'") + "')";

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
        String query =
                "INSERT INTO GUILD_PLAYLIST_TRACK (GUILD_PLAYLIST_ID, URL, TITLE) VALUES \n";

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
     * @param guildId guild id
     * @param playlistName playlist name
     * @return array of track
     */
    public ArrayList<Track> getTrackListFromPlaylist(long guildId, String playlistName)
    {
        String query =
                "SELECT GUILD_PLAYLIST_TRACK.URL, GUILD_PLAYLIST_TRACK.TITLE \n" +
                "FROM  GUILD_PLAYLIST_TRACK \n" +
                "JOIN GUILD_PLAYLIST ON GUILD_PLAYLIST_TRACK.GUILD_PLAYLIST_ID = GUILD_PLAYLIST.ID \n" +
                "WHERE GUILD_PLAYLIST.GUILD_ID = " + guildId + " AND GUILD_PLAYLIST.NAME = '" +
                playlistName.replace("'", "\\'") + "'";

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
                "FROM GUILD_PLAYLIST_TRACK " +
                "WHERE GUILD_PLAYLIST_ID = " + playlistId;

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
     * Get id from table guild playlist track
     * @param playlistId Playlist id
     * @param trackIndex track index
     * @return playlist track id
     */
    public long getPlaylistTrackId(long playlistId, int trackIndex)
    {
        String query =
                "SELECT ID " +
                "FROM GUILD_PLAYLIST_TRACK " +
                "WHERE GUILD_PLAYLIST_ID = " + playlistId + " ";

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
     * @param playlistTrackId guild_playlist_track id
     * @return true if track from playlist deleted.
     */
    public boolean deleteTrackFromPlaylistAsync(long playlistTrackId) throws SQLException
    {
        String query =
                "DELETE FROM GUILD_PLAYLIST_TRACK " +
                "WHERE ID = " + playlistTrackId + " ";

        return this.executeUpdateQuery(query) > 0;
    }
}

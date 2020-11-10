package database;

import database.Entity.GuildSetting;

import java.sql.*;

public class GuildModel extends BaseModel {

    public GuildModel() {
        super();
    }

    public void createNew(long id, boolean inDjMode, int defaultVolume, String customPrefix, int maxQueueLength,
                          int maxPlaylistCount, int maxSongDuration) throws SQLException {
        String query = "INSERT INTO GUILD (GUILD_ID, DJ_MODE, DEFAULT_VOLUME, CUSTOM_PREFIX, " +
                "MAX_QUEUE_LENGTH, MAX_PLAYLIST_COUNT, MAX_SONG_DURATION) " +
                "VALUES (" + id + ", " + inDjMode + ", " + defaultVolume + ", '" + customPrefix + "', " +
                maxQueueLength + ", " + maxPlaylistCount + ", " + maxSongDuration + ")";
        this.executeUpdateQuery(query);
    }

    public void createDefault(long id, String customPrefix) throws SQLException {
        String query = "INSERT INTO GUILD (GUILD_ID, CUSTOM_PREFIX) " +
                "VALUES (" + id + ", '" + customPrefix + "')";
        this.executeUpdateQuery(query);
    }

    public GuildSetting read(long id) throws SQLException {
        GuildSetting guildSetting = null;

        String query = "SELECT * FROM GUILD WHERE ID = " + id;
        try (Connection connection = DriverManager.getConnection(this.url,this.username,this.password)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        boolean djMode = result.getBoolean("DJ_MODE");
                        int defaultVolume = result.getInt("DEFAULT_VOLUME");
                        String customPrefix = result.getString("CUSTOM_PREFIX");
                        int maxQueueLength = result.getInt("MAX_QUEUE_LENGTH");
                        int maxPlaylistCount = result.getInt("MAX_PLAYLIST_COUNT");
                        int maxSongDuration = result.getInt("MAX_SONG_DURATION");
                        guildSetting = new GuildSetting(id, djMode, defaultVolume, customPrefix,
                                maxQueueLength, maxPlaylistCount, maxSongDuration);
                    }
                }
            }
        }

        return guildSetting;
    }
}

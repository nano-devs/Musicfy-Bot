package database;

import database.Entity.GuildSetting;

import java.sql.*;

public class GuildModel extends BaseModel {

    public GuildModel() {
        super();
    }

    public void createNew(long id, boolean inDjMode, int defaultVolume, String customPrefix) throws SQLException {
        String query = "INSERT INTO GUILD (GUILD_ID, DJ_MODE, DEFAULT_VOLUME, CUSTOM_PREFIX,) " +
                "VALUES (" + id + ", " + inDjMode + ", " + defaultVolume + ", '" + customPrefix + "', )";
        this.executeUpdateQuery(query);
    }

    public void createDefault(long id) throws SQLException {
        String query = "INSERT INTO GUILD (GUILD_ID) " +
                "VALUES (" + id + ")";
        this.executeUpdateQuery(query);
    }

    public GuildSetting read(long id) throws SQLException {
        GuildSetting guildSetting = null;

        String query = "SELECT * FROM GUILD JOIN GUILD_SETTINGS " +
                "ON GUILD.GUILD_SETTINGS_ID = GUILD_SETTINGS.GUILD_SETTINGS_ID " +
                "WHERE GUILD_ID = " + id;
        try (Connection connection = DriverManager.getConnection(this.url,this.username,this.password)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        boolean djMode = result.getBoolean("DJ_MODE");
                        int defaultVolume = result.getInt("DEFAULT_VOLUME");
                        String customPrefix = result.getString("CUSTOM_PREFIX");
                        int maxQueueLength = result.getInt("MAX_QUEUE_LENGTH");
                        int maxPlaylistCount = result.getInt("MAX_PLAYLIST_TRACK_COUNT");
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

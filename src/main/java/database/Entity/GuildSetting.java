package database.Entity;

import database.GuildModel;

import java.sql.*;

public class GuildSetting extends GuildModel {
    
    protected long id;
    protected boolean inDjMode;
    protected int defaultVolume;
    protected String customPrefix;
    protected int maxQueueLength;
    protected int maxPlaylistTrackCount;
    protected int maxSongDuration;
    protected boolean premium;

    private boolean canLoadSetting;

    public GuildSetting() {

    }

    public GuildSetting(long id, boolean inDjMode) {
        this.id = id;
        this.inDjMode = inDjMode;
        this.canLoadSetting = false;
    }

    public GuildSetting(long id, boolean inDjMode, int defaultVolume) {
        this.id = id;
        this.inDjMode = inDjMode;
        this.defaultVolume = defaultVolume;
        this.canLoadSetting = false;
    }

    public GuildSetting(long id, boolean inDjMode, int defaultVolume, String customPrefix, int maxQueueLength,
                        int maxPlaylistTrackCount, int maxSongDuration) {
        this.id = id;
        this.inDjMode = inDjMode;
        this.defaultVolume = defaultVolume;
        this.customPrefix = customPrefix;
        this.maxQueueLength = maxQueueLength;
        this.maxPlaylistTrackCount = maxPlaylistTrackCount;
        this.maxSongDuration = maxSongDuration;
        this.canLoadSetting = false;
    }

    public long getId() {
        return id;
    }

    public boolean isInDjMode() {
        return inDjMode;
    }

    public void setInDjMode(boolean inDjMode) {
        this.inDjMode = inDjMode;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void setDefaultVolume(int defaultVolume) {
        this.defaultVolume = defaultVolume;
    }

    public String getCustomPrefix() {
        return customPrefix;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public void setMaxQueueLength(int maxQueueLength) {
        this.maxQueueLength = maxQueueLength;
    }

    public int getMaxPlaylistTrackCount() {
        return maxPlaylistTrackCount;
    }

    public void setMaxPlaylistTrackCount(int maxPlaylistTrackCount) {
        this.maxPlaylistTrackCount = maxPlaylistTrackCount;
    }

    public int getMaxSongDuration() {
        return maxSongDuration;
    }

    public void setMaxSongDuration(int maxSongDuration) {
        this.maxSongDuration = maxSongDuration;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean canLoadSetting() {
        return canLoadSetting;
    }

    public void loadDefaultSetting(long id) {
        this.inDjMode = false;
        this.defaultVolume = 100;
        this.customPrefix = null;
        this.maxQueueLength = 60;
        this.maxPlaylistTrackCount = 20;
        this.maxSongDuration = 900000;
        this.canLoadSetting = false;

        try {
            this.createDefault(id);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void loadSetting(long id) throws SQLException {
        String query = "SELECT * FROM GUILD JOIN GUILD_SETTINGS " +
                "ON GUILD.GUILD_SETTINGS_ID = GUILD_SETTINGS.GUILD_SETTINGS_ID " +
                "WHERE GUILD_ID = " + id;

        try (Connection connection = DriverManager.getConnection(this.url,this.username,this.password)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        this.id = id;
                        this.inDjMode = result.getBoolean("DJ_MODE");
                        this.defaultVolume = result.getInt("DEFAULT_VOLUME");
                        this.customPrefix = result.getString("CUSTOM_PREFIX");
                        this.maxQueueLength = result.getInt("MAX_QUEUE_LENGTH");
                        this.maxPlaylistTrackCount = result.getInt("MAX_PLAYLIST_TRACK_COUNT");
                        this.maxSongDuration = result.getInt("MAX_SONG_DURATION");

                        this.canLoadSetting = true;
                    }
                }
            }
        }
    }

    public void saveCurrentPrefix(long id) throws SQLException {
        String query = "UPDATE GUILD SET CUSTOM_PREFIX = '" + this.customPrefix + "' WHERE GUILD_ID = " + id;
        this.executeUpdateQuery(query);
    }

    public void saveCurrentVolume(long id) throws SQLException {
        String query = "UPDATE GUILD SET DEFAULT_VOLUME = " + this.defaultVolume + " WHERE GUILD_ID = " + id;
        this.executeUpdateQuery(query);
    }

    public void saveCurrentDjMode(long id) throws SQLException {
        String query = "UPDATE GUILD SET DJ_MODE = " + this.isInDjMode() + " WHERE GUILD_ID = " + id;
        this.executeUpdateQuery(query);
    }
}

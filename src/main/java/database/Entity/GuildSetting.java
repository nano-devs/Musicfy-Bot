package database.Entity;

public class GuildSetting {
    
    private long id;
    private boolean inDjMode;
    private int defaultVolume;
    private String customPrefix;
    private int maxQueueLength;
    private int maxPlaylistCount;
    private long maxSongDuration;
    private boolean premium;

    public GuildSetting(long id, boolean inDjMode) {
        this.id = id;
        this.inDjMode = inDjMode;
    }

    public GuildSetting(long id, boolean inDjMode, int defaultVolume) {
        this.id = id;
        this.inDjMode = inDjMode;
        this.defaultVolume = defaultVolume;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getMaxPlaylistCount() {
        return maxPlaylistCount;
    }

    public void setMaxPlaylistCount(int maxPlaylistCount) {
        this.maxPlaylistCount = maxPlaylistCount;
    }

    public long getMaxSongDuration() {
        return maxSongDuration;
    }

    public void setMaxSongDuration(long maxSongDuration) {
        this.maxSongDuration = maxSongDuration;
    }
}

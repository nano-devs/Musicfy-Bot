package database.Entity;

public class GuildSetting {
    
    private long id;
    private boolean inDjMode;
    private int defaultVolume;

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
}

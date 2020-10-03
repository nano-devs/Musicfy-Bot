package client;

public class GuildSettings {
    private String prefix;
    private int defaultVolume;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void setDefaultVolume(int defaultVolume) {
        this.defaultVolume = defaultVolume;
    }
}

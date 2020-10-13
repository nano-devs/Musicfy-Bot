package client;

import net.dv8tion.jda.api.entities.Guild;

public class GuildSetting {

    private String guildId;
    private String prefix;
    private int defaultVolume;

    public GuildSetting(Guild guild) {
        this.guildId = guild.getId();
    }

    public String getGuildId() {
        return guildId;
    }

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

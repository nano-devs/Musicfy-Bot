package client;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomGuildSettingsManager implements GuildSettingsManager {

    private final Map<Long, GuildSetting> guildSettings;

    public CustomGuildSettingsManager() {
        this.guildSettings = new HashMap<>();
    }

    @Nullable
    @Override
    public Object getSettings(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildSetting guildSetting = guildSettings.get(guildId);

        if (guildSetting == null) {
            guildSetting = new GuildSetting(guild);
            guildSettings.put(guildId, guildSetting);
        }

        return guildSetting;
    }
}

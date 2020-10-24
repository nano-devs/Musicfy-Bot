package client;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomGuildSettingsManager implements GuildSettingsManager {

    private final Map<Long, database.Entity.GuildSetting> guildSettings;

    public CustomGuildSettingsManager() {
        this.guildSettings = new HashMap<>();
    }

    @Nullable
    @Override
    public Object getSettings(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        database.Entity.GuildSetting guildSetting = guildSettings.get(guildId);

        // if guild setting is not loaded on memory, then check database
        if (guildSetting == null) {
            // try load database from database.
            // ...

            // if no result. create new default guild setting & submit to database & return default setting
            // ...
            guildSettings.put(guildId, guildSetting);
        }

        return guildSetting;
    }
}

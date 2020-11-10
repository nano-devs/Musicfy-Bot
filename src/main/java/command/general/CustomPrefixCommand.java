package command.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

import java.sql.SQLException;

public class CustomPrefixCommand extends Command {

    public CustomPrefixCommand() {
        this.name = "set_prefix";
        this.arguments = "<new-prefix>";
        this.cooldown = 16;
        this.guildOnly = true;
        this.category = new Category("General");
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        String prefix = event.getArgs().trim();

        if (prefix.length() > 8) {
            event.reply(":x: | Cannot use prefix with characters length greater than 8");
            return;
        }

        musicManager.setCustomPrefix(prefix);

        try {
            musicManager.savePrefix(event.getGuild().getIdLong());
            event.reply(":white_check_mark: | Added new custom prefix: " + prefix);
        } catch (SQLException sqlException) {
            event.reply(":x: | Fail to set custom prefix: " + sqlException.getMessage());
        }
    }
}

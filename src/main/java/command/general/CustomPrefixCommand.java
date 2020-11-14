package command.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

import java.sql.SQLException;

public class CustomPrefixCommand extends Command {

    public CustomPrefixCommand() {
        this.name = "set_prefix";
        this.help = "Set guild custom prefix with maximum 8 characters length.";
        this.arguments = "<new-prefix>";
        this.cooldown = 16;
        this.guildOnly = true;
        this.category = new Category("General");
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        if (event.getArgs().isEmpty()) {
            event.reply(":x: | Invalid syntax: Argument for prefix is empty. Valid syntax: `" +
                    event.getClient().getPrefix() + "set_prefix <new-prefix>`");
            return;
        }

        String prefix = event.getArgs().replace(" ", "").trim();

        if (prefix.length() > 8) {
            event.reply(":x: | Cannot use prefix with characters length greater than 8");
            return;
        }

        if (prefix.contains("$")) {
            event.reply(":x: | Cannot use prefix that contains `$` or whitespaces");
            return;
        }

        musicManager.setCustomPrefix(prefix);

        try {
            musicManager.saveCurrentPrefix(event.getGuild().getIdLong());
            event.reply(":white_check_mark: | Custom prefix has been set to `" + prefix +
                    "`, you can test it using `" + prefix + "help`");
        } catch (SQLException sqlException) {
            event.reply(":x: | Fail to set custom prefix: " + sqlException.getMessage());
        }
    }
}

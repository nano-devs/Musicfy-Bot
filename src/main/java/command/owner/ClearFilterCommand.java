package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class ClearFilterCommand extends Command {

    public ClearFilterCommand() {
        this.name = "clear_filter";
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }
    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        musicManager.clearFilter();
        event.reply("Filter has been set to null!");
    }
}

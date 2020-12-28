package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class ApplyFlatFilterCommand extends Command {

    public ApplyFlatFilterCommand() {
        this.name = "apply_flat";
        this.category = new Category("Filter");
        this.guildOnly = true;
//        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        musicManager.applyFlatFilter();
        event.reply("Flat filter applied!");
    }
}

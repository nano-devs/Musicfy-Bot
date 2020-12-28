package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.filter.BassBoostFilter;

public class ApplyBassFilterCommand extends Command {

    public ApplyBassFilterCommand() {
        this.name = "apply_bass";
        this.category = new Category("Filter");
        this.guildOnly = true;
//        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        musicManager.applyBassBoostFilter();
        event.reply("Bass filter applied!");
    }
}

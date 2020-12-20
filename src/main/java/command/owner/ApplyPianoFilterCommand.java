package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.filter.PianoFilter;

public class ApplyPianoFilterCommand extends Command {

    public ApplyPianoFilterCommand() {
        this.name = "apply_piano";
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

        musicManager.applyPianoFilter();
        event.reply("Piano filter applied!");
    }
}

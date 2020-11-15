package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class NotifyMaintenanceCommand extends Command {
    public NotifyMaintenanceCommand() {
        this.name = "notify_mt";
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Guild> guilds = event.getJDA().getGuilds();

        for (Guild guild : guilds) {
            TextChannel defaultTextChannel = guild.getDefaultChannel();
            if (defaultTextChannel == null) {
                return;
            }
            try {
                defaultTextChannel.sendMessage(":warning: | Here is a 1-Hour advance notification " +
                        "for the coming maintenance period. Bot will be down for approximately 2 hours.\n" +
                        "Sorry for the inconvenience... ").queue();
            } catch (Exception e) {
                continue;
            }
        }
    }
}

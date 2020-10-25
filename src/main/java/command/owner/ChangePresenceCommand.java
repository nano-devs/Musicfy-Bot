package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Activity;

public class ChangePresenceCommand extends Command {

    public ChangePresenceCommand() {
        this.name = "c.p";
        this.aliases = new String[] {"c.presence", "change_presence"};
        this.ownerCommand = true;
        this.category = new Category("Owner");
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        String[] listOfArgs = args.trim().split(" ", 2);
        Activity temp;
        switch (listOfArgs[0]) {
            case "listening":
                temp = Activity.listening(listOfArgs[1]);
                break;
            case "playing":
                temp = Activity.playing(listOfArgs[1]);
                break;
            case "watching":
                temp = Activity.watching(listOfArgs[1]);
                break;
            default:
                temp = Activity.listening(event.getClient().getPrefix() + "help");
                break;
        }
        event.getJDA().getPresence().setActivity(temp);
        event.reply(":white_check_mark: | Presence changed to " + temp.toString());
    }
}

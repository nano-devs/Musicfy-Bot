package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.discordbots.api.client.DiscordBotListAPI;
import service.music.HelpProcess;

public class ClaimCommand extends Command {

    DiscordBotListAPI dblApi;

    public ClaimCommand(DiscordBotListAPI dblApi) {
        this.dblApi = dblApi;

        this.name="claim";
        this.category = new Category("Daily Event");
        this.help = "Vote and `claim` reward. Hope you like the reward :pray:";
        this.cooldown = 2;
        this.aliases = new String[] {"quota"};
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}

package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.MYSQL;

public class PremiumCommand extends Command
{
    private final MYSQL db;

    public PremiumCommand(MYSQL database)
    {
        this.db = database;

        this.name = "premium";
        this.aliases = new String[]{"prm"};
        this.guildOnly = false;
        this.cooldown = 2;
        this.help = "Become a premium user.";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        boolean result = this.db.addPremiumUser(event.getAuthor().getIdLong());
        if (result == true)
        {
           event.getChannel().sendMessage("You are premium now");
        }
        else
        {
            event.getChannel().sendMessage("Error to become premium");
        }
    }
}

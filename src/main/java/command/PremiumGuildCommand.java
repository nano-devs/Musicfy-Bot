package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumGuildModel;

public class PremiumGuildCommand extends Command
{
    public PremiumGuildCommand()
    {
        this.name = "premium guild";
        this.aliases = new String[]{"premg", "prem g", "prem guild"};
        this.ownerCommand = true;
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Become a premium guild for one month.";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        PremiumGuildModel db = new PremiumGuildModel();
        if (db.addPremiumGuild(event.getGuild().getIdLong()))
        {
            event.reply("This guild become premium for one month");
        }
        else
        {
            event.reply("The guild is already have a premium membership.");
        }
    }
}

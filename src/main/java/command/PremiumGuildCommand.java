package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumModel;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class PremiumGuildCommand extends Command
{
    private String table = "GUILD";

    public PremiumGuildCommand()
    {
//        this.name = "premium_guild";
        this.name = "premg";
//        this.aliases = new String[]{"premg", "prem g", "prem guild"};
        this.ownerCommand = true;
        this.guildOnly = true;
        this.cooldown = 2;
        this.help = "Become a premium guild for one month.";
        this.category = new Category("Owner");
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        PremiumModel db = new PremiumModel();
        CompletableFuture.runAsync(() ->
        {
            try
            {
                db.addPremiumAsync(event.getGuild().getIdLong(), this.table);
                event.reply("This guild become premium for one month");
            }
            catch (SQLException e)
            {
                event.reply("The guild is already have a premium membership.");
            }
        });
    }
}

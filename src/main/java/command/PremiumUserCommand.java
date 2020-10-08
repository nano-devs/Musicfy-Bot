package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumUserModel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PremiumUserCommand extends Command
{
    private final PremiumUserModel db;

    public PremiumUserCommand()
    {
        this.db = new PremiumUserModel();

        this.name = "premium user";
        this.aliases = new String[]{"premu", "prem u", "prem user"};
        this.ownerCommand = true;
        this.guildOnly = false;
        this.cooldown = 2;
        this.arguments = "<mentioned user>";
        this.help = "Become a premium user for one month.";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        List<Member> mention = event.getMessage().getMentionedMembers();

        if (mention.size() <= 0)
        {
            event.getChannel().sendMessage("No mentioned user").queue();
            return;
        }

        String user = "";
        for (int i = 0; i < mention.size(); i++)
        {
            boolean result = this.db.addPremiumUser(mention.get(i).getIdLong());
            if (result == true)
            {
                user += mention.get(i).getAsMention() + " ";
            }
        }

        if (user.length() > 1)
        {
            event.getChannel().sendMessage(user + "Become premium success.").queue();
        }
        else
        {
            event.getChannel().sendMessage("User is already premium").queue();
        }
    }
}

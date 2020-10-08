package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumUserModel;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class PremiumUserCommand extends Command
{
    public PremiumUserCommand()
    {
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
        String failed = "";

        PremiumUserModel db = new PremiumUserModel();
        for (Member member : mention)
        {
            if (db.addPremiumUser(member.getIdLong()))
            {
                user += member.getAsMention() + " ";
            }
            else
            {
                failed += member.getAsMention() + " ";
            }
        }

        if (user.length() > 1)
        {
            event.getChannel().sendMessage(user + "Become premium success.").queue();
        }
        else
        {
            event.getChannel().sendMessage(failed + " is already premium").queue();
        }
    }
}

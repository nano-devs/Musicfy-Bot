package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumUserModel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;

import java.sql.Connection;
import java.util.List;

public class PremiumUserCommand extends Command
{
    private final PremiumUserModel db;

    public PremiumUserCommand()
    {
        this.db = new PremiumUserModel();

        this.name = "premium user";
        this.aliases = new String[]{"prm"};
        this.guildOnly = false;
        this.cooldown = 2;
        this.arguments = "mentioned user";
        this.help = "Become a premium user for one month.";
    }

    public PremiumUserCommand(Connection connection)
    {
        this.db = new PremiumUserModel(connection);

        this.name = "premium";
        this.aliases = new String[]{"prm"};
        this.guildOnly = false;
        this.cooldown = 2;
        this.help = "Become a premium user for one month.";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if (event.isOwner() || this.db.isAdministrator(event.getAuthor().getIdLong()))
        {
            List<IMentionable> mention = event.getMessage().getMentions(Message.MentionType.USER);

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
                event.getChannel().sendMessage(user + "Become Premium succes.").queue();
            }
            else
            {
                event.getChannel().sendMessage("User is already premium").queue();
            }
        }
        else
        {
            event.reply("You're not administrator.");
        }
    }
}

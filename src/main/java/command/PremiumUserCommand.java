package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import database.PremiumModel;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PremiumUserCommand extends Command
{
    private String table = "USER";

    public PremiumUserCommand()
    {
//        this.name = "premium_user";
        this.name = "premu";
//        this.aliases = new String[]{"premu", "prem u", "prem user"};
        this.ownerCommand = true;
        this.guildOnly = false;
        this.cooldown = 2;
        this.arguments = "<mentioned user>";
        this.help = "Become a premium user for one month.";
        this.category = new Category("Owner");
        this.hidden = true;
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

        PremiumModel db = new PremiumModel();
        for (Member member : mention)
        {
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    db.addPremiumAsync(member.getIdLong(), this.table);
                    member.getUser().openPrivateChannel().queue(
                            msg -> msg.sendMessage("You become premium user."));
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }
}

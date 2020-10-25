package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HelpCommand extends Command
{
    private final NanoClient nano;
    private final List<Command> commandList;
    private Paginator.Builder paginate;

    public HelpCommand(NanoClient nano, List<Command> command)
    {
        this.nano = nano;
        this.commandList = command;
        this.create();

        this.name = "helps";
        this.aliases = new String[] { "h" };
        this.help = "Show all commands \n";
        this.cooldown = 2;
    }

    private void create()
    {
        this.paginate = new Paginator.Builder();
        this.paginate.setColumns(1);
        this.paginate.setItemsPerPage(10);
        this.paginate.showPageNumbers(true);
        this.paginate.waitOnSinglePage(false);
        this.paginate.setFinalAction(
                message ->
                {
                    try
                    {
                        message.clearReactions().queue();
                    }
                    catch (PermissionException e)
                    {
                        message.delete().queue();
                    }
                }
        );
        paginate.setEventWaiter(this.nano.getWaiter());
        paginate.setTimeout(1, TimeUnit.MINUTES);
    }

    private String split(String[] str)
    {
        String msg = str[0];

        if (str.length > 1)
        {
            for (int i = 1; i < str.length; i++)
            {
                msg += ", " + str[i];
            }
        }

        return msg;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        this.paginate.setUsers(event.getAuthor());
        this.paginate.setColor(new Color(0, 127, 255));
        this.paginate.setText("Help");

        Category category = this.commandList.get(0).getCategory();

        this.paginate.addItems("#__***" + category.getName() + "***__\n");

        for (Command command : this.commandList)
        {
            if (command.isOwnerCommand())
            {
                continue;
            }

            if (command.getCategory() == null)
            {
                this.paginate.addItems("\n#__***Uncategorized***__\n");
            }
            else if (!category.equals(command.getCategory()))
            {
                category = command.getCategory();
                this.paginate.addItems("#__***" + category.getName() + "***__\n");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Command: `" + command.getName() + "`\n");

            if (command.getAliases() != null && command.getAliases().length > 0)
            {
                sb.append("Alias : `" + this.split(command.getAliases()) + "`\n");
            }

            if (command.getArguments() !=  null)
            {
                sb.append("Arguments : `" + command.getArguments() + "`\n");
            }

            sb.append(command.getHelp() + "\n");
            this.paginate.addItems(sb.toString());
        }

        this.paginate.build().display(event.getChannel());
    }
}

package command.UserPlaylistCommand;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class UserPlaylistBaseCommand extends Command
{
    protected String table;

    public UserPlaylistBaseCommand()
    {
        this.table = "USER";
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}

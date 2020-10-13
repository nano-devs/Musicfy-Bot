package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class GuildPlaylistBaseCommand extends Command
{
    public final String table;

    public GuildPlaylistBaseCommand()
    {
        this.table = "GUILD";
    }

    @Override
    protected void execute(CommandEvent event)
    {

    }
}

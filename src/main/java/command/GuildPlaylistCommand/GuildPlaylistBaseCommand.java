package command.GuildPlaylistCommand;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class GuildPlaylistBaseCommand extends Command
{
    protected final String table;
    protected final int maxTrack = 5;
    protected final int maxPlaylist = 5;

    public GuildPlaylistBaseCommand()
    {
        this.table = "GUILD";
    }

    @Override
    protected void execute(CommandEvent event)
    {

    }
}

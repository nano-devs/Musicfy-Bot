package command.playlist.user;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class UserPlaylistBaseCommand extends Command
{
    protected String table;
    protected final int maxTrack = 5;
    protected final int maxPlaylist = 5;

    public UserPlaylistBaseCommand()
    {
        this.table = "USER";
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}

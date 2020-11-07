package command;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class DjModeCommand extends Command {

    private NanoClient nanoClient;

    public DjModeCommand(NanoClient nanoClient) {
        this.nanoClient = nanoClient;

        this.category = new Category("Music");
        this.name = "dj";
        this.aliases = new String[] {"dj_mode"};
        this.help = "Enable or disable Dj mode. Musicfy will react with :headphones: if enabled or :white_check_mark:" +
                " if disabled\n:warning: Only member with command `DJ` can use this command\n" +
                ":warning: If dj mode is enabled, only member with role `Dj` can request song or " +
                "change_volume/pause/resume/skip/join/leave";
        this.guildOnly = true;
        this.cooldown = 2;

        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());

        if (!MusicUtils.hasDjRole(event.getMember())) {
            event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
            return;
        }

        if (musicManager.isInDjMode()) {
            musicManager.setInDjMode(false);
            event.getMessage().addReaction("\u2705").queue();
        }
        else {
            musicManager.setInDjMode(true);
            event.getMessage().addReaction("\uD83C\uDFA7").queue();
        }
    }
}

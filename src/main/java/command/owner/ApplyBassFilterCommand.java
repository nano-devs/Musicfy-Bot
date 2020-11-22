package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.GuildMusicManager;

public class ApplyBassFilterCommand extends Command {
    public ApplyBassFilterCommand() {
        this.name = "ab";
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }
    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        String[] splitArgs = args.split(" ");

        float diff = Float.parseFloat(splitArgs[1]);

        GuildMusicManager musicManager = event.getClient().getSettingsFor(event.getGuild());

//        if (splitArgs[0].equals("h")) {
//            musicManager.getBassBoostFilter().applyHighBass(diff);
//            event.reply("Equalizer: HighBass filter applied with diff " + event.getArgs());
//        }
//        else {
//            musicManager.getBassBoostFilter().applyLowBass(diff);
//            event.reply("Equalizer: LowBass filter applied with diff " + event.getArgs());
//        }
    }
}

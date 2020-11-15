package command.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ShutdownCommand extends Command {
    public ShutdownCommand() {
        this.name = "s.d";
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {

        System.out.println("Shutting down scheduled thread pool executor!");
        event.getClient().getScheduleExecutor().shutdownNow();

        System.out.println("Shutting Down Command Client and JDA!");
        event.getClient().shutdown();
        event.getJDA().shutdownNow();

        System.out.println("Shut Down");
    }
}

package command;

import com.jagrosh.jdautilities.command.Command;

public abstract class CustomAbstractCommand extends Command {
    protected String example;

    public String getExample() {
        return example;
    }
}

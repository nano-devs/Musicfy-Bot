package service.music;

import com.jagrosh.jdautilities.command.Command;

public class HelpProcess
{
    public static String getHelp(Command command)
    {
        String prefix = "..";
        StringBuilder query = new StringBuilder();
        query.append("\n");
        if (command.getAliases() != null && command.getAliases().length > 0)
        {
            query.append("Alias: ");
            query.append("`");
            for (String alias : command.getAliases())
            {
                query.append(prefix + alias);
                query.append(" ");
            }
            query.append("`\n");
        }
        query.append("*");
        query.append(command.getHelp().replace("\n", ""));
        query.append("*");
        query.append("\n\n");
        return query.toString();
    }
}
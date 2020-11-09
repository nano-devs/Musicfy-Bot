package service.music;

import com.jagrosh.jdautilities.command.Command;

public class HelpProcess
{
    public static String getHelp(Command command)
    {
        String prefix = "m$";
        StringBuilder query = new StringBuilder();
        query.append("\n");
        if (command.getAliases() != null && command.getAliases().length > 0)
        {
            query.append("Alias(es): ");

            for (String alias : command.getAliases())
            {
                query.append("`");
                query.append(prefix + alias);
                query.append("` ");
            }
            query.append("\n\n");
        }
        query.append("*");
        query.append(command.getHelp().trim());
        query.append("*");
        query.append("\n\n");
        return query.toString();
    }

    public static CustomEmbedBuilder getCommandHelpDetail(Command command, String prefix, String altPrefix) {
        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();

        embedBuilder.setTitle(":bookmark: Help | " + command.getName());

        embedBuilder.setDescription(command.getHelp());

        String arguments = "";
        if (command.getArguments() != null) {
            arguments = " " + command.getArguments();
        }
        embedBuilder.addField("Usage",
                "```\n" + prefix + command.getName() + arguments  + "\n```", false);

        embedBuilder.setFooter("For more detail please checkout wiki!");

        return embedBuilder;
    }
}

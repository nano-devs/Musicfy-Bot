package command.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand extends Command {

    Map<String, Command> commandMap;

    public HelpCommand(CommandClient commandClient) {
        this.name = "help";
        this.arguments = "<command-name> (this argument is optional)";
        this.aliases = new String[] { "helps" };
        this.help = "Shows help & all commands";
        this.cooldown = 4;
        this.category = new Category("General");
        this.guildOnly = false;

        this.commandMap = new HashMap<>();

        this.initCommandMap(commandClient);
    }

    private void initCommandMap(CommandClient commandClient) {
        List<Command> commands = commandClient.getCommands();

        for (Command command : commands) {
            // Use memory to access the command in the future.
            commandMap.put(command.getName(), command);
            for (String alias : command.getAliases()) {
                commandMap.put(alias, command);
            }
        }
        commandMap.put("help", this);
    }

    private CustomEmbedBuilder getHelpEmbedBuilder(CommandClient commandClient, JDA jda, GuildMusicManager musicManager) {
        List<Command> commands = commandClient.getCommands();
        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();

        String customPrefix = "\nCustom Prefix: `" + musicManager.getCustomPrefix() + "`";

        if (musicManager.getCustomPrefix() == null || musicManager.getCustomPrefix().equals("null")) {
            customPrefix = "\nCustom Prefix: None, please use `set_prefix` command to add custom prefix";
        }

        // Theme Color: RGB(211, 0, 137)
        embedBuilder.setTitle("Musicfy Commands");
        embedBuilder.setDescription("Prefix: `" + commandClient.getPrefix() +
                "` | Alternative prefix: `" + commandClient.getAltPrefix() + "` the bot." + customPrefix);

        embedBuilder.addField("Helpful Links",
                "[Invite Musicfy](https://discord.com/api/oauth2/authorize?client_id=473023109666963467&permissions=36793408&scope=bot) " +
                        "- [Vote for Us!](https://top.gg/bot/473023109666963467/vote) " +
                        "- [Support Server](https://discord.gg/Y8sB4ay)",
                false);

        // Temporary memory
        Map<String, String> dictionary = new HashMap<>();

        for (Command command : commands) {
            String currentCategoryName = "**" + command.getCategory().getName() + "**";
            String categoryValue = "`" + command.getName() + "`, ";

            // Use the temporary memory for Main Embed
            if (currentCategoryName.equals("**Owner**"))
                continue;

            if (!dictionary.containsKey(currentCategoryName)) {
                dictionary.put(currentCategoryName, categoryValue);
                continue;
            }

            // Append command name to temporary memory
            dictionary.put(currentCategoryName, dictionary.get(currentCategoryName) + categoryValue);
        }

        for (String key : dictionary.keySet()) {
            String values = dictionary.get(key);
            embedBuilder.addField(key, values.substring(0, values.length() - 2), false);
        }

        embedBuilder.addField("Detail",
                "For more detail try `" + commandClient.getPrefix() + "help <command-name>`", false);

        embedBuilder.setFooter("For additional help, contact Made Y#8195",
                jda.getSelfUser().getEffectiveAvatarUrl());

        return embedBuilder;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.getArgs().isEmpty()) {
            String commandName = event.getArgs();

            if (!commandMap.containsKey(commandName)) {
                event.reply(":x: | Command with name `" + commandName +
                        "` does not exist.. please use `" + event.getClient().getPrefix() +
                        "help` to get the list of command names");
                return;
            }

            Command requestedCommand = commandMap.get(commandName);

            event.reply(HelpProcess.getCommandHelpDetail(
                    requestedCommand,
                    event.getClient().getPrefix(),
                    event.getClient().getAltPrefix()).build());

            return;
        }

        event.reply(this.getHelpEmbedBuilder(event.getClient(), event.getJDA(),
                event.getClient().getSettingsFor(event.getGuild())).build());
    }
}

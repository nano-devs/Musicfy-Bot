package command.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import service.music.CustomEmbedBuilder;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand extends Command {

    MessageEmbed messageEmbed;

    public HelpCommand(CommandClient commandClient, JDA jda) {
        this.name = "help";
        this.aliases = new String[] { "helps" };
        this.help = "Shows help & all commands";
        this.cooldown = 2;
        this.category = new Category("General");
        this.guildOnly = false;

        this.messageEmbed = this.createHelpMessageEmbed(commandClient, jda);
    }

    private MessageEmbed createHelpMessageEmbed(CommandClient commandClient, JDA jda) {
        List<Command> commands = commandClient.getCommands();

        EmbedBuilder embedBuilder = new CustomEmbedBuilder();

        // Theme Color: RGB(211, 0, 137)
        embedBuilder.setTitle("Musicfy Commands");
        embedBuilder.setDescription("Prefix: `" + commandClient.getPrefix() +
                "` | Alternative prefix: `" + commandClient.getAltPrefix() + "` the bot.");

        Map<String, String> dictionary = new HashMap<>();

        for (Command command : commands) {
            String currentCategoryName = "**" + command.getCategory().getName() + "**";
            String categoryValue = "`" + command.getName() + "`, ";

            if (currentCategoryName.equals("Owner"))
                continue;

            if (!dictionary.containsKey(currentCategoryName)) {
                dictionary.put(currentCategoryName, categoryValue);
                continue;
            }
            dictionary.put(currentCategoryName, dictionary.get(currentCategoryName) + categoryValue);
        }

        for (String key : dictionary.keySet()) {
            String values = dictionary.get(key);
            embedBuilder.addField(key, values.substring(0, values.length() - 2), false);
        }

        embedBuilder.addField("Detail",
                "For more detail try `" + commandClient.getPrefix() + "help <command-name>`", false);

        embedBuilder.setFooter("If you need additional help, contact Made Y#8195",
                jda.getSelfUser().getEffectiveAvatarUrl());

        return embedBuilder.build();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(this.messageEmbed);
    }
}

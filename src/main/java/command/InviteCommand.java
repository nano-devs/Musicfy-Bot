package command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class InviteCommand extends Command {

    public InviteCommand() {

    }

    @Override
    protected void execute(CommandEvent event) {
        String inviteLink = "https://discord.com/api/oauth2/authorize?client_id=473023109666963467&permissions=36793408&scope=bot";
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(":headphones: | Thank you for using _ bot");
        embedBuilder.addField("Here is the invite link!", inviteLink, true);
        embedBuilder.setFooter(":love_letter: | Have a nice dayy~");
        embedBuilder.setThumbnail(event.getSelfUser().getAvatarUrl());
        event.reply(embedBuilder.build());
    }
}

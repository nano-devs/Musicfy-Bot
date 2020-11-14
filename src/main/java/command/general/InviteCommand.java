package command.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import service.music.CustomEmbedBuilder;
import service.music.HelpProcess;

public class InviteCommand extends Command {

    public InviteCommand() {
        this.name="invite";
        this.category = new Category("General");
        this.help = "Get invite link.";
        this.cooldown = 2;
        this.aliases = new String[] {"link", "invite_link"};
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        String inviteLink = "[invite link](https://discord.com/api/oauth2/authorize?client_id=473023109666963467&permissions=36793408&scope=bot)";
        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
        embedBuilder.setAuthor("Invite " + event.getSelfUser().getName(),
                event.getAuthor().getEffectiveAvatarUrl(),
                event.getAuthor().getEffectiveAvatarUrl());
        embedBuilder.setTitle(":headphones: | Thank you for using " + event.getSelfUser().getName() + "!");
        embedBuilder.setFooter("Have a nice dayy~");
        embedBuilder.addField("Here is the invite link!", inviteLink, true);
        embedBuilder.setThumbnail(event.getSelfUser().getAvatarUrl());
        event.reply(embedBuilder.build());
    }
}

package listener;

import com.jagrosh.jdautilities.command.CommandClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.discordbots.api.client.DiscordBotListAPI;
import org.jetbrains.annotations.NotNull;
import service.music.CustomEmbedBuilder;

public class GuildEventListener extends ListenerAdapter {

    private DiscordBotListAPI dblApi;
    private CommandClient commandClient;

    public GuildEventListener(DiscordBotListAPI api, CommandClient commandClient) {
        this.dblApi = api;
        this.commandClient = commandClient;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        JDA jda = event.getJDA();

        this.dblApi.setStats(jda.getGuilds().size());

        TextChannel defaultTextChannel = event.getGuild().getDefaultChannel();
        if (defaultTextChannel == null) {
            return;
        }

        CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();

        embedBuilder.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        embedBuilder.setTitle(":headphones: | Thanks for using Musicfy!");
        embedBuilder.setDescription("Please use `m$help` to get started!");

        String links = "";
        links += "[Musicfy Invite Link](https://discord.com/api/oauth2/authorize?client_id=473023109666963467&permissions=36793408&scope=bot)\n";
        links += "[Wiki](https://github.com/nano-devs/Musicfy-Bot/wiki)\n";
        links += "[Patreon](https://www.patreon.com/musicfy)";
        embedBuilder.addField("Links", links, false);
        embedBuilder.setFooter("Have a nice dayy~");

        defaultTextChannel.sendMessage(embedBuilder.build()).queue();
        
        // Notify owner. development purposes
//        String messageToOwner = "Just left server " + event.getGuild().getName()
//                + ". Total " + jda.getGuilds().size() + " guilds";
//
//        User owner = jda.getUserById(this.commandClient.getOwnerId());
//
//        owner.openPrivateChannel().flatMap(channel -> channel.sendMessage(messageToOwner)).queue();
        super.onGuildJoin(event);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {

        super.onGuildLeave(event);

        JDA jda = event.getJDA();

        this.dblApi.setStats(jda.getGuilds().size());

        // Notify owner. development purposes
//        String messageToOwner = "Joined server " + event.getGuild().getName()
//                + ". Total " + jda.getGuilds().size() + " guilds";
//
//        User owner = jda.getUserById(this.commandClient.getOwnerId());
//
//        owner.openPrivateChannel().flatMap(channel -> channel.sendMessage(messageToOwner)).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        JDA jda = event.getJDA();

        this.dblApi.setStats(jda.getGuilds().size());
    }
}

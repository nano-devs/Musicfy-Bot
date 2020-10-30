package listener;

import com.jagrosh.jdautilities.command.CommandClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.discordbots.api.client.DiscordBotListAPI;
import org.jetbrains.annotations.NotNull;

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
        JDA jda = event.getJDA();

        this.dblApi.setStats(jda.getGuilds().size());

        // Notify owner. development purposes
//        String messageToOwner = "Joined server " + event.getGuild().getName()
//                + ". Total " + jda.getGuilds().size() + " guilds";
//
//        User owner = jda.getUserById(this.commandClient.getOwnerId());
//
//        owner.openPrivateChannel().flatMap(channel -> channel.sendMessage(messageToOwner)).queue();

        super.onGuildLeave(event);
    }
}

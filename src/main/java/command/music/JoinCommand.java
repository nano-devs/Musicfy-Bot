package command.music;

import client.NanoClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import service.music.CustomEmbedBuilder;
import service.music.GuildMusicManager;
import service.music.HelpProcess;
import service.music.MusicUtils;

public class JoinCommand extends Command {

    NanoClient nanoClient;

    public JoinCommand(NanoClient nanoClient) {
        this.name = "join";
        this.help = "Join user's voice channel";
        this.aliases = new String[]{"summon"};
        this.guildOnly = true;
        this.nanoClient = nanoClient;
        this.category = new Category("Music");
        this.help = HelpProcess.getHelp(this);
    }

    @Override
    protected void execute(CommandEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            event.reply(":x: | You're not connected to any voice channel");
            return;
        }
        
        GuildMusicManager musicManager = nanoClient.getGuildAudioPlayer(event.getGuild());
        musicManager.scheduler.textChannel = event.getTextChannel();

        if (musicManager.isInDjMode()) {
            if (!MusicUtils.hasDjRole(event.getMember())) {
                event.reply(MusicUtils.getDjModeEmbeddedWarning(event.getMember()).build());
                return;
            }
        }
        event.getGuild().getAudioManager().openAudioConnection(channel);

        if (!event.getSelfMember().hasPermission(Permission.VOICE_DEAF_OTHERS) &&
            !event.getSelfMember().getVoiceState().isDeafened()) {
            CustomEmbedBuilder embedBuilder = new CustomEmbedBuilder();
            embedBuilder.addField(":warning: Missing Permission: `Deafen Members`!",
                    "Please don't undeafen me! I work better by being deafened because: " +
                            "Less lag, more clear, better quality, and doesn't randomly disconnect",
                    true);
            event.reply(embedBuilder.build());
        }

        event.reply(":white_check_mark: | Connected to :loud_sound: `" + channel.getName() + "`");
    }
}

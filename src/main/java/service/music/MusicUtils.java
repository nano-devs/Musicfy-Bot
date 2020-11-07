package service.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class MusicUtils {
    /**
     *
     * @param durationMillis
     * @return String formatted duration
     */
    public static String getDurationFormat(long durationMillis){
        int seconds = (int) durationMillis / 1000;
        String format = "";
        if (seconds > 3600) {
            format = String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
        }
        else {
            format = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
        }
        return format;
    }

    /**
     *
     * @param durationMillis
     * @return Formatted Minute
     */
    public static String getMinuteFormat(long durationMillis){
        String durationMinute = String.valueOf( (int)(durationMillis/1000)/60 );
        String durationSecond = String.valueOf( (int)(durationMillis/1000)%60 );
        if (durationMinute.length() == 1) durationMinute = "0" + durationMinute;
        if (durationSecond.length() == 1) durationSecond = "0" + durationSecond;
        return durationMinute + ":" + durationSecond;
    }

    public static boolean hasDjRole(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getName().toUpperCase().equals("DJ")) {
                return true;
            }
        }
        return false;
    }

    public static EmbedBuilder getDjModeEmbeddedWarning(Member member) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(member.getColor());
        embedBuilder.setAuthor(member.getUser().getName(),
                member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        embedBuilder.addField(":warning: Dj mode is on.",
                "Only member with role `DJ` can modify the AudioPlayer state", true);
        embedBuilder.setFooter("Dj mode will automatically be turned off when bot leave the voice channel");

        return embedBuilder;
    }
}

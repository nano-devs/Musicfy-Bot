package service.music.filter;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class BassBoostFilter {
    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f };

    private final EqualizerFactory equalizer;
    private final AudioPlayer player;

    public BassBoostFilter(AudioPlayer player) {
        this.equalizer = new EqualizerFactory();
        this.player = player;
    }

    public void applyFilters() {

    }
}

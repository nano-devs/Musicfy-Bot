package service.music.filter;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import java.util.Collections;

public class BassBoostFilter {

    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f };

    private final EqualizerFactory equalizer;
    private final AudioPlayer player;

    public BassBoostFilter(AudioPlayer player) {
        this.equalizer = new EqualizerFactory();
        this.player = player;
        this.player.setFilterFactory(equalizer);
    }

    public void applyHighBass(float diff) {
        for (int i = 0; i < BASS_BOOST.length; i++) {
            equalizer.setGain(i, BASS_BOOST[i] + diff);
        }
    }

    public void applyLowBass(float diff) {
        for (int i = 0; i < BASS_BOOST.length; i++) {
            equalizer.setGain(i, -BASS_BOOST[i] + diff);
        }
    }
}

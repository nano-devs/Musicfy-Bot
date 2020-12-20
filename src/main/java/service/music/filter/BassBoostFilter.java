package service.music.filter;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import java.util.Collections;

public class BassBoostFilter {

    private static final float[] BASS_BOOST = {
            -0.075f, 0.125f, 0.125f, 0.1f, 0.1f,
            0.05f, 0.075f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.125f, 0.15f, 0.05f
    };

    private EqualizerFactory equalizer;

    public BassBoostFilter() {
        equalizer = new EqualizerFactory();
        apply(equalizer);
    }

    public static void apply(EqualizerFactory equalizer) {
        for (int i = 0; i < BASS_BOOST.length; i++) {
            equalizer.setGain(i, BASS_BOOST[i]);
        }
    }

    public EqualizerFactory getEqualizer() {
        return equalizer;
    }
}

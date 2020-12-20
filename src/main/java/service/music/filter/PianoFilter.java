package service.music.filter;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class PianoFilter {
    private static final float[] PIANO = {
            -0.25f, -0.25f, -0.125f, 0.0f,
            0.25f, 0.25f, 0.0f, -0.25f, -0.25f,
            0.0f, 0.0f, 0.5f, 0.25f, -0.025f };

    private final EqualizerFactory equalizer;

    public PianoFilter() {
        equalizer = new EqualizerFactory();
        apply(equalizer);
    }

    public static void apply(EqualizerFactory equalizer) {
        for (int i = 0; i < PIANO.length; i++) {
            equalizer.setGain(i, PIANO[i]);
        }
    }

    public EqualizerFactory getEqualizer() {
        return equalizer;
    }
}

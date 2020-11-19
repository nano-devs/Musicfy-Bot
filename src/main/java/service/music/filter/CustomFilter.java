package service.music.filter;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class CustomFilter {
    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f };

    private final EqualizerFactory equalizer;

    public CustomFilter() {
        this.equalizer = new EqualizerFactory();
    }


}

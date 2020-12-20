package service.music.filter;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class BaseFilter {
    protected float[] BANDS;
    private static EqualizerFactory equalizer;

    public BaseFilter() {
        equalizer = new EqualizerFactory();
    }

    protected void setBANDS(float[] BANDS) {
        this.BANDS = BANDS;
        this.apply();
    }

    public float[] getBANDS() {
        return BANDS;
    }

    public EqualizerFactory getEqualizer() {
        return equalizer;
    }

    public void apply() {
        for (int i = 0; i < BANDS.length; i++) {
            equalizer.setGain(i, BANDS[i]);
        }
    }
}

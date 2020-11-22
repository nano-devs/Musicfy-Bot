package service.music.setting;

public enum BoostSetting {
    OFF(0.0F, 0.0F),
    SOFT(0.25F, 0.15F),
    HARD(0.50F, 0.25F),
    EXTREME(0.75F, 0.50F),
    MINDBEND(1F, 0.75F);

    public final float band1;
    public final float band2;

    BoostSetting(float band1, float band2) {
        this.band1 = band1;
        this.band2 = band2;
    }
}

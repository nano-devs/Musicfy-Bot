package service.music.filter;

public class FlatFilter extends BaseFilter{

    public FlatFilter() {
        float[] flatBands = {
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
        };
        this.setBANDS(flatBands);
    }
}

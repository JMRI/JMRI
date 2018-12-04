package jmri.jmrit.timetable;

public enum Scale {
    G    (22.5f),
    O43  (43.0f),
    O45  (45.0f),
    O48  (48.0f),
    S    (64.0f),
    OO   (76.2f),
    HO   (87.1f),
    TT   (120.0f),
    N148 (148.0f),
    N    (160.0f),
    Z    (220.0f);

    private final float _ratio;

    Scale(float ratio) {
        _ratio = ratio;
    }

    public float getRatio() {
        return _ratio;
    }

    @Override
    public String toString() {
        return String.format("%2s (%.1f)", name(), getRatio());
    }
}

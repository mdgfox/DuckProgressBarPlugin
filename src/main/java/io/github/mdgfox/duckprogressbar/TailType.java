package io.github.mdgfox.duckprogressbar;

/** What is drawn behind the duck. */
public enum TailType {
    NONE("None"),
    BUBBLES("Bubbles"),
    WAVE("Wave");

    private final String displayName;

    TailType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

package io.github.mdgfox.duckprogressbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

/** Application-level plugin settings, edited on the Settings page. */
@Service(Service.Level.APP)
@State(name = "DuckProgressBarSettings", storages = @Storage("DuckProgressBar.xml"))
public final class DuckSettings implements PersistentStateComponent<DuckSettings.State> {

    /**
     * Serialized form. Enums are stored by name, so a value that no longer exists
     * (for example a skin or tail from an older version) falls back to the default.
     */
    public static class State {
        public boolean enabled = true;
        public String skin = Skin.DUCK.name();
        public String tail = TailType.NONE.name();
        public boolean tailInDeterminate = false;
    }

    private State state = new State();

    public static DuckSettings getInstance() {
        return ApplicationManager.getApplication().getService(DuckSettings.class);
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public boolean isEnabled() {
        return state.enabled;
    }

    public void setEnabled(boolean enabled) {
        state.enabled = enabled;
    }

    public Skin getSkin() {
        return parse(Skin.class, state.skin, Skin.DUCK);
    }

    public void setSkin(Skin skin) {
        state.skin = skin.name();
    }

    public TailType getTail() {
        return parse(TailType.class, state.tail, TailType.NONE);
    }

    public void setTail(TailType tail) {
        state.tail = tail.name();
    }

    /** Whether the tail is also painted on bars that show a percentage, not only on the indeterminate ones. */
    public boolean isTailInDeterminate() {
        return state.tailInDeterminate;
    }

    public void setTailInDeterminate(boolean tailInDeterminate) {
        state.tailInDeterminate = tailInDeterminate;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return fallback;
        }
    }
}

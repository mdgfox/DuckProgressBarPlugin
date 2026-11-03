package io.github.mdgfox.duckprogressbar;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Installs {@link DuckProgressBarUI} on startup and re-installs it after every theme change,
 * because switching the Look and Feel resets the UIManager defaults.
 */
public class ProgressBarListener implements LafManagerListener, AppLifecycleListener {
    private static final String UI_KEY = "ProgressBarUI";

    /** The progress bar UI of the current Look and Feel, used when the duck is switched off. */
    private static volatile Object originalUi;

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        installProgressBarUi();
    }

    @Override
    public void lookAndFeelChanged(@NotNull LafManager lafManager) {
        installProgressBarUi();
    }

    /** Applies the settings to the UIManager and to the progress bars that are already on screen. */
    static void reinstall() {
        installProgressBarUi();
        for (Window window : Window.getWindows()) {
            updateProgressBars(window);
        }
    }

    private static void installProgressBarUi() {
        String duckUi = DuckProgressBarUI.class.getName();
        Object current = UIManager.get(UI_KEY);
        if (current != null && !duckUi.equals(current)) {
            originalUi = current;
        }

        if (DuckSettings.getInstance().isEnabled()) {
            UIManager.put(UI_KEY, duckUi);
            UIManager.getDefaults().put(duckUi, DuckProgressBarUI.class);
        } else if (originalUi != null) {
            UIManager.put(UI_KEY, originalUi);
        }
    }

    private static void updateProgressBars(Component component) {
        if (component instanceof JProgressBar progressBar) {
            progressBar.updateUI();
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                updateProgressBars(child);
            }
        }
    }
}

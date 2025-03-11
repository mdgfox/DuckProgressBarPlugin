package main.java.com.duckprogressbarplugin;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProgressBarListener implements LafManagerListener, ApplicationActivationListener {

    public ProgressBarListener() {}

    @Override
    public void lookAndFeelChanged(@NotNull LafManager lafManager) {
        // see https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html
        updateProgressBarUi();
    }

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        updateProgressBarUi();
    }

    private static void updateProgressBarUi() {
        UIManager.put("ProgressBarUI", ProgressBarUI.class.getName());
        UIManager.getDefaults().put(ProgressBarUI.class.getName(), ProgressBarUI.class);
    }
}
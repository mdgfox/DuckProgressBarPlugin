package com.example.duckprogressbarplugin;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProgressBarListener implements LafManagerListener {

    public ProgressBarListener() {
        LafManager.getInstance().addLafManagerListener(__ -> updateProgressBarUi());
        updateProgressBarUi();
    }

    @Override
    public void lookAndFeelChanged(@NotNull LafManager lafManager) {
        updateProgressBarUi();
    }

    private static void updateProgressBarUi() {
        UIManager.put("ProgressBarUI", ProgressBarUI.class.getName());
        UIManager.getDefaults().put(ProgressBarUI.class.getName(), ProgressBarUI.class);
    }
}
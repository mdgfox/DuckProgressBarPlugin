package io.github.mdgfox.duckprogressbar;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

/** Starts a 10 second background task, so the progress bar can be seen without waiting for a real one. */
public abstract class PreviewProgressAction extends AnAction implements DumbAware {
    private static final int STEPS = 100;
    private static final int STEP_MILLIS = 100;

    private final boolean indeterminate;

    protected PreviewProgressAction(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String title = indeterminate ? "Duck preview (indeterminate)" : "Duck preview (determinate)";
        ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), title, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(indeterminate);
                try {
                    for (int step = 1; step <= STEPS; step++) {
                        indicator.checkCanceled();
                        indicator.setText2(step * STEP_MILLIS / 1000 + " s");
                        if (!indeterminate) {
                            indicator.setFraction(step / (double)STEPS);
                        }
                        Thread.sleep(STEP_MILLIS);
                    }
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ProcessCanceledException(ex);
                }
            }
        });
    }

    public static class Indeterminate extends PreviewProgressAction {
        public Indeterminate() {
            super(true);
        }
    }

    public static class Determinate extends PreviewProgressAction {
        public Determinate() {
            super(false);
        }
    }
}

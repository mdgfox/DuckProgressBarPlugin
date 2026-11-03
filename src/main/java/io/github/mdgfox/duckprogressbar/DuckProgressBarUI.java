package io.github.mdgfox.duckprogressbar;

import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.IdentityHashMap;
import java.util.Map;

public class DuckProgressBarUI extends BasicProgressBarUI {
    private static final Color BACKGROUND_COLOR = new Color(255, 209, 82, 38);

    /** Duck speed in unscaled pixels per second. */
    private static final float DUCK_SPEED = 60f;

    /** Gap in unscaled pixels between the duck and the right end of the bar, so the beak doesn't go under the frame. */
    private static final int EDGE_GAP = 3;

    /** Size of a rolling character relative to the bar height. */
    private static final float ROLLING_SCALE = 0.8f;

    /** Vertical position of the rolling character's own center within its icon (the pumpkin body is below the stem). */
    private static final float ROLLING_PIVOT_Y = 0.58f;

    private final long animationStartNanos = System.nanoTime();

    // Scaled copies of the duck icons, so we don't create new ones on every repaint
    private final Map<Icon, Icon> scaledIcons = new IdentityHashMap<>();
    private int scaledIconsHeight;
    private float scaledIconsSysScale;

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        c.setBorder(JBUI.Borders.empty().asUIResource());
        return new DuckProgressBarUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(super.getPreferredSize(c).width, JBUIScale.scale(20));
    }

    @Override
    protected void paintIndeterminate(Graphics graphics, JComponent component) {
        if (!(graphics instanceof Graphics2D)) {
            return;
        }
        Graphics2D g = (Graphics2D)graphics;

        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        g.setColor(new JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50)));
        int w = component.getWidth();
        int h = component.getPreferredSize().height;
        if (isNotEven(component.getHeight() - h)) h++;


        g.setPaint(BACKGROUND_COLOR);

        if (component.isOpaque()) {
            g.fillRect(0, (component.getHeight() - h)/2, w, h);
        }
        g.setColor(new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50)));
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        try {
            g.translate(0, (component.getHeight() - h) / 2);

            Paint old = g.getPaint();
            g.setPaint(BACKGROUND_COLOR);

            final float R = JBUIScale.scale(8f);
            final float R2 = JBUIScale.scale(9f);
            final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R));
            g.fill(containingRoundRect);
            g.setPaint(old);

            // Ping-pong position derived from elapsed time, so the speed doesn't depend on the repaint rate
            final int duckSize = h;
            final int edgeGap = JBUIScale.scale(EDGE_GAP);
            final int minOffset = duckSize / 2;
            final int maxOffset = Math.max(minOffset + 1, w - duckSize / 2 - edgeGap);
            final float travel = maxOffset - minOffset;
            final DuckSettings settings = DuckSettings.getInstance();
            final float seconds = secondsSinceStart();
            final float distance = seconds * JBUIScale.scale(DUCK_SPEED);
            final float phase = distance % (2 * travel);
            final boolean movingRight = phase < travel;
            final int offset = minOffset + Math.round(movingRight ? phase : 2 * travel - phase);

            Area area = new Area(new Rectangle2D.Float(0, 0, w, h));
            area.subtract(new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R)));
            g.setPaint(Gray._128);
            if (component.isOpaque()) {
                g.fill(area);
            }

            area.subtract(new Area(new RoundRectangle2D.Float(0, 0, w, h, R2, R2)));

            Container parent = component.getParent();
            Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
            g.setPaint(background);

            if (component.isOpaque()) {
                g.fill(area);
            }

            Shape barInside = new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R);
            TailPainter.paint(g, settings.getTail(), offset, duckSize, movingRight, seconds, barInside);
            paintCharacter(g, settings.getSkin(), movingRight, offset, duckSize, offset);

            g.draw(new RoundRectangle2D.Float(1f, 1f, w - 2f - 1f, h - 2f -1f, R, R));
            g.translate(0, -(component.getHeight() - h) / 2);

            // Handle possible text painting
            if (progressBar.isStringPainted()) {
                if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
                    paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width);
                }
                else {
                    paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.y, boxRect.height);
                }
            }
        }
        finally {
            config.restore();
        }
    }

    @Override
    protected void paintDeterminate(Graphics graphics, JComponent component) {
        if (!(graphics instanceof Graphics2D)) {
            return;
        }
        Graphics2D g = (Graphics2D)graphics;


        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL || !component.getComponentOrientation().isLeftToRight()) {
            super.paintDeterminate(graphics, component);
            return;
        }
        Insets b = progressBar.getInsets();
        int w = progressBar.getWidth();
        int h = progressBar.getPreferredSize().height;
        if (isNotEven(component.getHeight() - h)) h++;

        int barRectWidth = w - (b.right + b.left);
        int barRectHeight = h - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        final GraphicsConfig config = GraphicsUtil.setupAAPainting(graphics);
        try {
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            Container parent = component.getParent();
            Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

            graphics.setColor(background);
            if (component.isOpaque()) {
                graphics.fillRect(0, 0, w, h);
            }

            final float R = JBUIScale.scale(8f);
            final float R2 = JBUIScale.scale(9f);
            final float off = JBUIScale.scale(1f);

            g.translate(0, (component.getHeight() - h)/2);
            g.setColor(progressBar.getForeground());
            g.fill(new RoundRectangle2D.Float(0, 0, w - off, h - off, R2, R2));
            g.setColor(background);
            g.fill(new RoundRectangle2D.Float(off, off, w - 2f*off - off, h - 2f*off - off, R, R));
            g.setPaint(BACKGROUND_COLOR);

            DuckSettings settings = DuckSettings.getInstance();
            Skin skin = settings.getSkin();
            int duckWidth = fitToHeight(skin.getIcon(true), h).getIconWidth();
            int edgeGap = JBUIScale.scale(EDGE_GAP);
            int duckX = Math.max(0, Math.min(amountFull - duckWidth / 2, w - duckWidth - edgeGap));
            int duckCenterX = duckX + duckWidth / 2;
            Shape barInside = new RoundRectangle2D.Float(off, off, w - 2f*off - off, h - 2f*off - off, R, R);
            if (settings.isTailInDeterminate()) {
                TailPainter.paint(g, settings.getTail(), duckCenterX, h, true, secondsSinceStart(), barInside);
            }
            paintCharacter(g, skin, true, duckCenterX, h, amountFull);
            g.fill(new RoundRectangle2D.Float(2f*off,2f*off, amountFull - JBUIScale.scale(5f), h - JBUIScale.scale(5f), JBUIScale.scale(7f), JBUIScale.scale(7f)));
            g.translate(0, -(component.getHeight() - h)/2);

            if (progressBar.isStringPainted()) {
                paintString(graphics, b.left, b.top,
                        barRectWidth, barRectHeight,
                        amountFull, b);
            }
        }
        finally {
            config.restore();
        }
    }

    private void paintString(Graphics g, int x, int y, int w, int h, int fillStart, int amountFull) {
        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(g2, progressString,
                x, y, w, h);
        Rectangle oldClip = g2.getClipBounds();

        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
            g2.setColor(getSelectionBackground());
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
        } else { // VERTICAL
            g2.setColor(getSelectionBackground());
            AffineTransform rotate =
                    AffineTransform.getRotateInstance(Math.PI/2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(g2, progressString,
                    x, y, w, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x, fillStart, w, amountFull);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    private float secondsSinceStart() {
        return (System.nanoTime() - animationStartNanos) / 1_000_000_000f;
    }

    /**
     * Paints the character centered at {@code centerX}.
     *
     * @param traveled how far the character has moved along the bar in pixels, rolling skins turn by this distance
     */
    private void paintCharacter(Graphics2D g, Skin skin, boolean movingRight, int centerX, int size, float traveled) {
        if (!skin.isRolling()) {
            Icon icon = fitToHeight(skin.getIcon(movingRight), size);
            icon.paintIcon(progressBar, g, centerX - icon.getIconWidth() / 2, 0);
            return;
        }

        // A rolling character is smaller and turns around its own center, so it stays inside the bar while rotating
        int rollSize = Math.round(size * ROLLING_SCALE);
        Icon icon = fitToHeight(skin.getIcon(movingRight), rollSize);
        float pivotY = size / 2f;
        int x = centerX - icon.getIconWidth() / 2;
        int y = Math.round(pivotY - icon.getIconHeight() * ROLLING_PIVOT_Y);
        Graphics2D rotated = (Graphics2D)g.create();
        try {
            rotated.rotate(traveled / (rollSize / 2f), centerX, pivotY);
            icon.paintIcon(progressBar, rotated, x, y);
        }
        finally {
            rotated.dispose();
        }
    }

    /**
     * Scales the icon so it is exactly {@code height} pixels tall and isn't clipped by the bar.
     * The result is cached until the requested height or the screen scale changes.
     */
    private Icon fitToHeight(Icon icon, int height) {
        if (height <= 0 || icon.getIconHeight() <= 0) {
            return icon;
        }
        float sysScale = JBUIScale.sysScale(progressBar);
        if (height != scaledIconsHeight || sysScale != scaledIconsSysScale) {
            scaledIcons.clear();
            scaledIconsHeight = height;
            scaledIconsSysScale = sysScale;
        }
        return scaledIcons.computeIfAbsent(icon,
                source -> IconUtil.scale(source, progressBar, (float)height / source.getIconHeight()));
    }

    private static boolean isNotEven(int value) {
        return value % 2 != 0;
    }
}

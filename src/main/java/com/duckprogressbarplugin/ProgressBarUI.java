package main.java.com.duckprogressbarplugin;

import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.ScalableIcon;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ProgressBarUI extends BasicProgressBarUI {
    private volatile int offset2 = 0;
    private volatile int velocity = 1;
    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent component) {
        component.setBorder(JBUI.Borders.empty().asUIResource());
        return new ProgressBarUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        return new Dimension(super.getPreferredSize(component).width, JBUIScale.scale(20));
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        progressBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
            }
        });
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

        if ((component.getHeight() - h) % 2 != 0) {
            h++;
        }

        g.setPaint(getBackgroundFill(h));

        if (component.isOpaque()) {
            g.fillRect(0, (component.getHeight() - h)/2, w, h);
        }
        g.setColor(new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50)));
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        g.translate(0, (component.getHeight() - h) / 2);

        Paint old = g.getPaint();
        g.setPaint(getBackgroundFill(h));

        final float R = JBUIScale.scale(8f);
        final float R2 = JBUIScale.scale(9f);
        final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R));
        g.fill(containingRoundRect);
        g.setPaint(old);

        offset2 += velocity;

        if (offset2 <= 2) {
            offset2 = 2;
            velocity = 1;
        } else if (offset2 >= w - JBUIScale.scale(15)) {
            offset2 = w - JBUIScale.scale(15);
            velocity = -1;
        }
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

        Icon scaledIcon = velocity > 0 ? ((ScalableIcon) Icons.duckIcon) : ((ScalableIcon) Icons.reverseDuck) ;
        scaledIcon.paintIcon(progressBar, g, offset2 - JBUIScale.scale(10), -JBUIScale.scale(6));

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
        config.restore();
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

        final GraphicsConfig config = GraphicsUtil.setupAAPainting(graphics);
        Insets b = progressBar.getInsets();
        int w = progressBar.getWidth();
        int h = progressBar.getPreferredSize().height;
        if ((component.getHeight() - h) % 2 != 0) h++;

        int barRectWidth = w - (b.right + b.left);
        int barRectHeight = h - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

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
        g.setPaint(getBackgroundFill(h));

        Icons.duckIcon.paintIcon(progressBar, g, amountFull - JBUIScale.scale(10), -JBUIScale.scale(6));
        g.fill(new RoundRectangle2D.Float(2f*off,2f*off, amountFull - JBUIScale.scale(5f), h - JBUIScale.scale(5f), JBUIScale.scale(7f), JBUIScale.scale(7f)));
        g.translate(0, -(component.getHeight() - h)/2);

        if (progressBar.isStringPainted()) {
            paintString(graphics, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
        }
        config.restore();
    }

    private void paintString(Graphics g, int x, int y, int w, int h, int fillStart, int amountFull) {
        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
        Rectangle oldClip = g2.getClipBounds();

        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
            g2.setColor(getSelectionBackground());
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
        } else {
            g2.setColor(getSelectionBackground());
            AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI/2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(g2, progressString, x, y, w, h);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x, fillStart, w, amountFull);
            BasicGraphicsUtils.drawString(progressBar, g2, progressString, renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    private LinearGradientPaint getBackgroundFill(float scaledHeight) {
        float epsilon = 0.000001f;

        float[] fractionList = new float[Background.COLORS.length * 2];
        Color[] colorList = new Color[Background.COLORS.length * 2];

        for (int i = 0; i < Background.COLORS.length; i++) {
            fractionList[i * 2] = (float) i / Background.COLORS.length;
            fractionList[i * 2 + 1] = ((i + 1) - epsilon) / Background.COLORS.length;

            colorList[i * 2] = Background.COLORS[i];
            colorList[i * 2 + 1] = Background.COLORS[i];
        }

        return new LinearGradientPaint(0, JBUI.scale(1), 0, scaledHeight - JBUI.scale(3), fractionList, colorList);
    }
}

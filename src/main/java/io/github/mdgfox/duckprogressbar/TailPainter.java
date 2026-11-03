package io.github.mdgfox.duckprogressbar;

import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/** Paints the trail the duck leaves behind it. */
final class TailPainter {
    private static final Color TAIL_COLOR = new JBColor(new Color(0x3E8FD0), new Color(0x6CB6F0));

    private TailPainter() {
    }

    /**
     * @param centerX     x of the duck's center
     * @param barHeight   height of the bar, the duck has the same size
     * @param movingRight the trail is drawn on the opposite side
     * @param seconds     animation time
     * @param clip        the inside of the bar, nothing is painted outside of it
     */
    static void paint(Graphics2D g, TailType type, int centerX, int barHeight, boolean movingRight, float seconds, Shape clip) {
        if (type == TailType.NONE) {
            return;
        }
        Graphics2D t = (Graphics2D)g.create();
        try {
            t.clip(clip);
            float direction = movingRight ? -1f : 1f;
            float length = barHeight * 2.5f;
            // The trail starts behind the duck's body
            float startX = centerX + direction * barHeight * 0.3f;
            switch (type) {
                case BUBBLES -> paintBubbles(t, startX, direction, length, barHeight, seconds);
                case WAVE -> paintWave(t, startX, direction, length, barHeight, seconds);
                default -> {
                }
            }
        }
        finally {
            t.dispose();
        }
    }

    private static void paintBubbles(Graphics2D g, float startX, float direction, float length, int h, float seconds) {
        final int count = 7;
        for (int i = 0; i < count; i++) {
            // age goes from 0 (just emitted) to 1 (gone), every bubble has its own phase
            float age = (seconds * 0.9f + i / (float)count) % 1f;
            float x = startX + direction * age * length;
            float rise = age * h * 0.25f;
            float y = h * (0.55f + 0.25f * fraction(i * 0.618f)) - rise
                    + (float)Math.sin(seconds * 4f + i * 2f) * JBUIScale.scale(1f);
            float radius = h * (0.09f + 0.06f * fraction(i * 0.37f)) * (1f - 0.5f * age);
            int alpha = Math.round(190 * (1f - age));

            Shape bubble = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
            g.setColor(withAlpha(TAIL_COLOR,alpha / 3));
            g.fill(bubble);
            g.setColor(withAlpha(TAIL_COLOR,alpha));
            g.setStroke(new BasicStroke(Math.max(1f, JBUIScale.scale(0.8f))));
            g.draw(bubble);
        }
    }

    private static void paintWave(Graphics2D g, float startX, float direction, float length, int h, float seconds) {
        g.setStroke(new BasicStroke(Math.max(1f, JBUIScale.scale(1.6f)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float step = Math.max(2f, JBUIScale.scale(2f));
        float waveLength = h * 0.9f;
        float amplitude = h * 0.12f;
        for (int line = 0; line < 2; line++) {
            float baseY = h * (0.72f - 0.2f * line);
            float phaseShift = line * 1.7f;
            float prevX = startX;
            float prevY = baseY + (float)Math.sin(-seconds * 6f + phaseShift) * amplitude;
            for (float d = step; d <= length; d += step) {
                float u = d / length;
                float x = startX + direction * d;
                float y = baseY + amplitude * (float)Math.sin(d / waveLength * 2 * Math.PI - seconds * 6f + phaseShift);
                g.setColor(withAlpha(TAIL_COLOR,Math.round((line == 0 ? 200 : 120) * (1f - u))));
                g.draw(new Line2D.Float(prevX, prevY, x, y));
                prevX = x;
                prevY = y;
            }
        }
    }

    private static float fraction(float value) {
        return value - (float)Math.floor(value);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}

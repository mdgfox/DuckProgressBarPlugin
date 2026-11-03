package io.github.mdgfox.duckprogressbar;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/** A character that swims (or rolls) along the progress bar. */
public enum Skin {
    DUCK("Duck", Group.SVG, "/skins/duck.svg", "/skins/duck_left.svg", false),
    VAMPIRE("Vampire duck", Group.SVG, "/skins/vampire.svg", "/skins/vampire_left.svg", false),
    PUMPKIN("Jack-o'-lantern", Group.SVG, "/skins/pumpkin.svg", "/skins/pumpkin.svg", true),
    CLASSIC("Classic duck", Group.PNG, "/duck.png", "/r_duck.png", false);

    /** How the skins are grouped on the Settings page. */
    public enum Group {
        SVG("SVG characters"),
        PNG("Classic (PNG)");

        private final String title;

        Group(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    private final String displayName;
    private final Group group;
    private final Icon right;
    private final Icon left;
    private final boolean rolls;

    Skin(String displayName, Group group, String rightPath, String leftPath, boolean rolls) {
        this.displayName = displayName;
        this.group = group;
        this.right = IconLoader.getIcon(rightPath, Skin.class);
        this.left = IconLoader.getIcon(leftPath, Skin.class);
        this.rolls = rolls;
    }

    public Group getGroup() {
        return group;
    }

    /** The icon facing the direction of movement. */
    public Icon getIcon(boolean movingRight) {
        return movingRight ? right : left;
    }

    /** Rolling skins rotate while moving instead of turning around. */
    public boolean isRolling() {
        return rolls;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

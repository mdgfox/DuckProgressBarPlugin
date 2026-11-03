package io.github.mdgfox.duckprogressbar;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Settings | Appearance & Behavior | Duck Progress Bar. */
public class DuckSettingsConfigurable implements Configurable {
    private JBCheckBox enabledCheckBox;
    private final Map<Skin, JBRadioButton> skinButtons = new EnumMap<>(Skin.class);
    private ComboBox<TailType> tailComboBox;
    private JBCheckBox tailInDeterminateCheckBox;

    @Override
    public @Nls String getDisplayName() {
        return "Duck Progress Bar";
    }

    @Override
    public @Nullable JComponent createComponent() {
        enabledCheckBox = new JBCheckBox("Replace the progress bar with the duck");

        tailComboBox = new ComboBox<>(TailType.values());
        tailInDeterminateCheckBox = new JBCheckBox("Apply to determinate bar");
        JPanel tailPanel = new JPanel();
        tailPanel.setLayout(new BoxLayout(tailPanel, BoxLayout.X_AXIS));
        tailPanel.add(tailComboBox);
        tailPanel.add(Box.createHorizontalStrut(12));
        tailPanel.add(tailInDeterminateCheckBox);
        tailPanel.add(Box.createHorizontalGlue());

        FormBuilder form = FormBuilder.createFormBuilder()
                .addComponent(enabledCheckBox)
                .addLabeledComponent("Tail:", tailPanel);
        for (JComponent section : createSkinSections()) {
            form.addComponent(section);
        }

        reset();
        return form.addComponentFillVertically(new JPanel(), 0).getPanel();
    }

    /** One titled section per {@link Skin.Group}, all radio buttons share a single selection. */
    private List<JComponent> createSkinSections() {
        ButtonGroup buttonGroup = new ButtonGroup();
        List<JComponent> sections = new ArrayList<>();
        for (Skin.Group group : Skin.Group.values()) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(IdeBorderFactory.createTitledBorder(group.getTitle()));
            for (Skin skin : Skin.values()) {
                if (skin.getGroup() != group) {
                    continue;
                }
                JBRadioButton button = new JBRadioButton(skin.toString());
                button.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                buttonGroup.add(button);
                skinButtons.put(skin, button);
                panel.add(button);
            }
            sections.add(panel);
        }
        return sections;
    }

    @Override
    public boolean isModified() {
        DuckSettings settings = DuckSettings.getInstance();
        return enabledCheckBox.isSelected() != settings.isEnabled()
                || getSelectedSkin() != settings.getSkin()
                || tailComboBox.getItem() != settings.getTail()
                || tailInDeterminateCheckBox.isSelected() != settings.isTailInDeterminate();
    }

    @Override
    public void apply() {
        DuckSettings settings = DuckSettings.getInstance();
        settings.setEnabled(enabledCheckBox.isSelected());
        settings.setSkin(getSelectedSkin());
        settings.setTail(tailComboBox.getItem());
        settings.setTailInDeterminate(tailInDeterminateCheckBox.isSelected());
        ProgressBarListener.reinstall();
    }

    @Override
    public void reset() {
        DuckSettings settings = DuckSettings.getInstance();
        enabledCheckBox.setSelected(settings.isEnabled());
        skinButtons.get(settings.getSkin()).setSelected(true);
        tailComboBox.setItem(settings.getTail());
        tailInDeterminateCheckBox.setSelected(settings.isTailInDeterminate());
    }

    @Override
    public void disposeUIResources() {
        enabledCheckBox = null;
        skinButtons.clear();
        tailComboBox = null;
        tailInDeterminateCheckBox = null;
    }

    private Skin getSelectedSkin() {
        for (Map.Entry<Skin, JBRadioButton> entry : skinButtons.entrySet()) {
            if (entry.getValue().isSelected()) {
                return entry.getKey();
            }
        }
        return Skin.DUCK;
    }
}

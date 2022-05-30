package jmri.script.swing;

import javax.swing.JComboBox;

import jmri.script.ScriptEngineSelector;
import jmri.script.ScriptEngineSelector.Engine;

/**
 * A JComboBox for selecting a valid scripting engine.
 *
 * Persistence of settings, if desired, should be handled in the using class.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2021, 2022
 */
public class ScriptEngineSelectorSwing {

    private final JComboBox<Engine> comboBox =
            new JComboBox<>(ScriptEngineSelector.getAllEngines().toArray(new Engine[0]));

    private final ScriptEngineSelector _selector;

    public ScriptEngineSelectorSwing(ScriptEngineSelector selector) {
        _selector = selector;
        updateSetComboBoxSelection();
    }

    public final void updateSetComboBoxSelection() {
        comboBox.setSelectedItem(_selector.getSelectedEngine());
    }

    public JComboBox<Engine> getComboBox() {
        return comboBox;
    }

    public void update() {
        _selector.setSelectedEngine(comboBox.getItemAt(comboBox.getSelectedIndex()));
    }

}

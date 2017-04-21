package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the LOAD_MODIFIER macro from the Digitrax sound definition
 * language
 *
 * Arg1: Upper 4 bits - math modifiers FMATH_LODE et al Arg2: Arg3:
 *
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
class LoadModifierEditor extends SdfMacroEditor {

    public LoadModifierEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
}

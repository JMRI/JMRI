package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the generic two-byte macros from the Digitrax sound
 * definition language
 * <p>
 * In theory, this should never be invoked, because all the macros have specific
 * editors. But editors like this never keep up with changes to content of
 * underlying data, so we provide this as an escape claus for unrecognized
 * content.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008
 */
class TwoByteMacroEditor extends SdfMacroEditor {

    public TwoByteMacroEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }

}

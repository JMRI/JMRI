// SkipOnTriggerEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SKIP_ON_TRIGGER macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class SkipOnTriggerEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -7444256530247471680L;

    public SkipOnTriggerEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
}

/* @(#)SkipOnTriggerEditor.java */

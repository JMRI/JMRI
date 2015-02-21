// GenerateTriggerEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the GENERATE_TRIGGER macro from the Digitrax sound
 * definition language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class GenerateTriggerEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -4809270548383400410L;

    public GenerateTriggerEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }

}

/* @(#)GenerateTriggerEditor.java */

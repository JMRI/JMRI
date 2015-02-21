// DelaySoundEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the DELAY_SOUND macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class DelaySoundEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = 8584517409405023951L;

    public DelaySoundEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }

}

/* @(#)DelaySoundEditor.java */

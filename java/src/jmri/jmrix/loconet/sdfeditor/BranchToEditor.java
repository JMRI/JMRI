// BranchToEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the BRANCH_TO macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class BranchToEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -7625740509332461282L;

    public BranchToEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
}

/* @(#)BranchToEditor.java */

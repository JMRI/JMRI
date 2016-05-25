// LabelMacroEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SdfMacro for carrying a comment
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class LabelMacroEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -3358357325534061565L;

    public LabelMacroEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }

}

/* @(#)LabelMacroEditor.java */

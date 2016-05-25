// MaskCompareEditor.java
package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the MASK_COMPARE macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
class MaskCompareEditor extends SdfMacroEditor {

    /**
     *
     */
    private static final long serialVersionUID = -1823359711169229533L;

    public MaskCompareEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
}

/* @(#)MaskCompareEditor.java */

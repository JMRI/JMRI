// GenerateTriggerEditor.java

package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;
import javax.swing.JLabel;

/**
 * Editor panel for the GENERATE_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision$
 */

class GenerateTriggerEditor extends SdfMacroEditor {

    public GenerateTriggerEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();
        
        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
    
}

/* @(#)GenerateTriggerEditor.java */

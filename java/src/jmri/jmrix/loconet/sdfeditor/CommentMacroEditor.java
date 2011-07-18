// CommentMacroEditor.java

package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.SdfMacro;
import javax.swing.JLabel;

/**
 * Editor panel for the SdfMacro for carrying a comment
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision$
 */

class CommentMacroEditor extends SdfMacroEditor {

    public CommentMacroEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();
        
        // and set up our own
        add(new JLabel("No editor defined for this instruction yet."));
    }
}

/* @(#)CommentMacroEditor.java */

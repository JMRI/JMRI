package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the SdfMacro for carrying a comment
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
class CommentMacroEditor extends SdfMacroEditor {

    public CommentMacroEditor(SdfMacro inst) {
        super(inst);

        // remove default message from SdfMacroEditor
        this.removeAll();

        // and set up our own
        add(new JLabel("No editor defined for this instruction yet.")); // NOI18N
    }
}

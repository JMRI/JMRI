package jmri.jmrit.ctc.editor;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a CtcEditor
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class CtcEditorAction extends JmriAbstractAction {

    public CtcEditorAction(String s) {
        super(s);
    }

    public CtcEditorAction() {
        this(Bundle.getMessage("CtcEditorActionButton"));  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jmri.InstanceManager.getNullableDefault(CtcEditor.class) != null) {
            // Prevent duplicate copies
            return;
        }
        jmri.util.JmriJFrame f = new jmri.jmrit.ctc.editor.gui.FrmMainForm();
//         jmri.util.JmriJFrame f = new jmri.jmrit.ctc.editor.CtcEditor();
        f.addHelpMenu("html.tools.TimeTable", true);  // NOI18N
        f.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }
}

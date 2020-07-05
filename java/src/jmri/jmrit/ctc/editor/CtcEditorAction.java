package jmri.jmrit.ctc.editor;

import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrit.ctc.editor.gui.FrmMainForm;
import jmri.util.swing.JmriAbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a CtcEditor.
 *
 * @author Dave Sand Copyright (C) 2019
 */
@API(status = MAINTAINED)
public class CtcEditorAction extends JmriAbstractAction {

    public CtcEditorAction(String s) {
        super(s);
    }

    public CtcEditorAction() {
        this(Bundle.getMessage("CtcEditorActionButton"));  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getOptionalDefault(FrmMainForm.class).orElseGet(() -> new FrmMainForm())
                .setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }

}

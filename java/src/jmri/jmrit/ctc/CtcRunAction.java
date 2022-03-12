package jmri.jmrit.ctc;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;

/**
 * Swing action to create and register the CTC Run Time.
 * Replaces the original custom file opener.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunAction extends JmriAbstractAction {

    public CtcRunAction(String s) {
        super(s);
    }

    public CtcRunAction() {
        this(Bundle.getMessage("CtcRunActionButton"));  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jmri.InstanceManager.getNullableDefault(CTCMain.class) != null) {
            // Prevent duplicate copies
            return;
        }
        CTCMain _mCTCMain = new CTCMain();
        jmri.InstanceManager.setDefault(CTCMain.class, _mCTCMain);
        _mCTCMain.startup();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }

}

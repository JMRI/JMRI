package jmri.jmrit.ctc;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register the CTC Run Time
 *
 * @author Dave Sand Copyright (C) 2018
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
        if (jmri.InstanceManager.getNullableDefault(CtcRun.class) != null) {
            // Prevent duplicate copies
            return;
        }
        CTCMain _mCTCMain = new CTCMain();
        String filename = jmri.util.FileUtil.getUserFilesPath() + "ctc/CTCSystem.xml";
        log.info("file name = {}", filename);
        _mCTCMain.readDataFromXMLFile(filename);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunAction.class);
}

package jmri.jmrit.ctc;

import jmri.InstanceManager;

public class CtcRun {

    public CtcRun() {
        InstanceManager.setDefault(CtcRun.class, this);
        // Find and load run time data, invoke file open if necessary
        log.info("CTC Run Time Ready");  // NOI18N
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRun.class);
}
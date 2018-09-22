package jmri.jmrix.ecos.swing.monitor;

import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.swing.EcosPanelInterface;

/**
 * Swing action to create and register a MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class EcosMonPane extends jmri.jmrix.AbstractMonPane implements EcosListener, EcosPanelInterface {

    public EcosMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return memo.getUserName() + " Command Monitor";
        }
        return "ECoS Command Monitor";
    }

    @Override
    public void dispose() {
        // disconnect from the ECosTrafficController
        memo.getTrafficController().removeEcosListener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    EcosSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof EcosSystemConnectionMemo) {
            initComponents((EcosSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(EcosSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        memo.getTrafficController().addEcosListener(this);
    }

    @Override
    public synchronized void message(EcosMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            logMessage("binary cmd: ",l);
        } else {
            logMessage("cmd: ",l);
        }
    }

    @Override
    public synchronized void reply(EcosReply l) {  // receive a reply message and log it
        if (l.isUnsolicited()) {
            logMessage("msg: ",l);
        } else {
            logMessage("rep: ",l);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.ecos.swing.EcosNamedPaneAction {

        public Default() {
            super("ECoS Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    EcosMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(EcosSystemConnectionMemo.class));
        }
    }

}

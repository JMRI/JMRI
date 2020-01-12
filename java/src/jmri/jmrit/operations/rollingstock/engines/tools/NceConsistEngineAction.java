package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Starts the NceConsistEngine thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class NceConsistEngineAction extends AbstractAction {

    NceTrafficController tc;

    public NceConsistEngineAction(String actionName, Component frame) {
        super(actionName);
        // only enable if connected to an NCE system
        setEnabled(false);
        // disable if NCE USB selected
        // get NceTrafficContoller if there's one
        List<NceSystemConnectionMemo> memos = InstanceManager.getList(NceSystemConnectionMemo.class);

        // find NceConnection that is serial
        for (int i = 0; i < memos.size(); i++) {
            NceSystemConnectionMemo memo = memos.get(i);
            if (memo.getNceUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
                tc = memo.getNceTrafficController();
                if (!memo.getDisabled()) {
                    setEnabled(true);
                }
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Thread mb = new NceConsistEngines(tc);
        mb.setName("Nce Consist Sync Engines"); // NOI18N
        mb.start();
    }

//    private final static Logger log = LoggerFactory.getLogger(NceConsistEngineAction.class);
}

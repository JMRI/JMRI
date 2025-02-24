package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Egbert Broerse Copyright 2025
 */
public class Lnsv1ProgAction extends LnNamedPaneAction {

    public Lnsv1ProgAction() {
        super(Bundle.getMessage("MenuItemLnsv1Prog"),
                new JmriJFrameInterface(),
                Lnsv1ProgPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

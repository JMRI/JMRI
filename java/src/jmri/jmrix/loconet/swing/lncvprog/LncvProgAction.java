package jmri.jmrix.loconet.swing.lncvprog;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Egbert Broerse Copyright 2020
 */
public class LncvProgAction extends LnNamedPaneAction {

    public LncvProgAction() {
        super(Bundle.getMessage("MenuItemLncvProg"),
                new JmriJFrameInterface(),
                LncvProgPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

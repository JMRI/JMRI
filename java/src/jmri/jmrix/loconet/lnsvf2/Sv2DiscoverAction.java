package jmri.jmrix.loconet.lnsvf2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locomon.LocoMonPane;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Egbert Broerse Copyright 2020
 */
public class Sv2DiscoverAction extends LnNamedPaneAction {

    public Sv2DiscoverAction() {
        super(Bundle.getMessage("MenuItemDiscoverSv2"),
                new JmriJFrameInterface(),
                Sv2DiscoverPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

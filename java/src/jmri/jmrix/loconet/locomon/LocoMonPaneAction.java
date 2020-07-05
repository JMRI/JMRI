package jmri.jmrix.loconet.locomon;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = EXPERIMENTAL)
public class LocoMonPaneAction extends LnNamedPaneAction {

    public LocoMonPaneAction() {
        super(Bundle.getMessage("MenuItemLocoNetMonitor"),
                new JmriJFrameInterface(),
                LocoMonPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

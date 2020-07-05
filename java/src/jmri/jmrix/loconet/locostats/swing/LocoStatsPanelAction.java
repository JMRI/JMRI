package jmri.jmrix.loconet.locostats.swing;

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
public class LocoStatsPanelAction extends LnNamedPaneAction {

    public LocoStatsPanelAction() {
        super(Bundle.getMessage("MenuItemLocoStats"),
                new JmriJFrameInterface(),
                LocoStatsPanel.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

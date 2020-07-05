package jmri.jmrix.tams.swing.statusframe;

import jmri.InstanceManager;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.swing.TamsNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = EXPERIMENTAL)
public class StatusPanelAction extends TamsNamedPaneAction {

    public StatusPanelAction() {
        super(Bundle.getMessage("MenuItemInfo"),
                new JmriJFrameInterface(),
                StatusPanel.class.getName(),
                InstanceManager.getDefault(TamsSystemConnectionMemo.class));
    }
}

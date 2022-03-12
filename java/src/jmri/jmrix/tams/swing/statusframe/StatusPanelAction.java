package jmri.jmrix.tams.swing.statusframe;

import jmri.InstanceManager;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.swing.TamsNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class StatusPanelAction extends TamsNamedPaneAction {

    public StatusPanelAction() {
        super(Bundle.getMessage("MenuItemInfo"),
                new JmriJFrameInterface(),
                StatusPanel.class.getName(),
                InstanceManager.getDefault(TamsSystemConnectionMemo.class));
    }
}

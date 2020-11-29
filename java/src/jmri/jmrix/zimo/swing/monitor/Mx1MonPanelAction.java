package jmri.jmrix.zimo.swing.monitor;

import jmri.InstanceManager;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.swing.Mx1NamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class Mx1MonPanelAction extends Mx1NamedPaneAction {

    public Mx1MonPanelAction() {
        super("Mx1 Command Monitor",
                new JmriJFrameInterface(),
                Mx1MonPanel.class.getName(),
                InstanceManager.getDefault(Mx1SystemConnectionMemo.class));
    }
}

package jmri.jmrix.openlcb.swing.networktree;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Swing action to create and register a MonitorFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012
 */
public class NetworkTreeAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public NetworkTreeAction() {
        super("Openlcb Network Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                NetworkTreePane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }
}

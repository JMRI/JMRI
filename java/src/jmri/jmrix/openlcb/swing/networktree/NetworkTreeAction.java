package jmri.jmrix.openlcb.swing.networktree;

import jmri.jmrix.can.CanSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a MonitorFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012
 */
@API(status = EXPERIMENTAL)
public class NetworkTreeAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public NetworkTreeAction() {
        super("Openlcb Network Tree",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                NetworkTreePane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }
}

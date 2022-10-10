package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Swing action to create and register a MonitorFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010
 */
public class MonitorAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public MonitorAction() {
        super(Bundle.getMessage("MonitorTitle"),
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                MonitorPane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }
}

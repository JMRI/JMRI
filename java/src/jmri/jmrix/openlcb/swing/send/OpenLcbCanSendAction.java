package jmri.jmrix.openlcb.swing.send;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Create and register a tool to send OpenLCB CAN frames.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class OpenLcbCanSendAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public OpenLcbCanSendAction() {
        super("Send CAN Frames and OpenLCB Messages",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                OpenLcbCanSendPane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }
}

package jmri.jmrix.powerline.swing.serialmon;

import jmri.InstanceManager;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.swing.PowerlineNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class SerialMonPaneAction extends PowerlineNamedPaneAction {

    public SerialMonPaneAction() {
        super("Open Powerline Monitor",
                new JmriJFrameInterface(),
                SerialMonPane.class.getName(),
                InstanceManager.getDefault(SerialSystemConnectionMemo.class));
    }

}

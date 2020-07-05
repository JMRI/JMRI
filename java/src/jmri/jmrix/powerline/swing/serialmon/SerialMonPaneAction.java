package jmri.jmrix.powerline.swing.serialmon;

import jmri.InstanceManager;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.swing.PowerlineNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = EXPERIMENTAL)
public class SerialMonPaneAction extends PowerlineNamedPaneAction {

    public SerialMonPaneAction() {
        super("Open Powerline Monitor",
                new JmriJFrameInterface(),
                SerialMonPane.class.getName(),
                InstanceManager.getDefault(SerialSystemConnectionMemo.class));
    }

}

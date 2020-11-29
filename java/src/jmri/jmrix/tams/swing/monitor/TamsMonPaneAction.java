package jmri.jmrix.tams.swing.monitor;

import jmri.InstanceManager;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.swing.TamsNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class TamsMonPaneAction extends TamsNamedPaneAction {

    public TamsMonPaneAction() {
        super(Bundle.getMessage("CommandMonitor"),
                new JmriJFrameInterface(),
                TamsMonPane.class.getName(),
                InstanceManager.getDefault(TamsSystemConnectionMemo.class));
    }
}

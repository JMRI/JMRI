package jmri.jmrix.tams.swing.monitor;

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
public class TamsMonPaneAction extends TamsNamedPaneAction {

    public TamsMonPaneAction() {
        super(Bundle.getMessage("CommandMonitor"),
                new JmriJFrameInterface(),
                TamsMonPane.class.getName(),
                InstanceManager.getDefault(TamsSystemConnectionMemo.class));
    }
}

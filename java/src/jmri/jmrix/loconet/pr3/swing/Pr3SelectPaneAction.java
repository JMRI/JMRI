package jmri.jmrix.loconet.pr3.swing;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = EXPERIMENTAL)
public class Pr3SelectPaneAction extends LnNamedPaneAction {

    public Pr3SelectPaneAction() {
        super(Bundle.getMessage("MenuItemPr3ModeSelect"),
                new JmriJFrameInterface(),
                Pr3SelectPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

package jmri.jmrix.loconet.pr3.swing;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class Pr3SelectPaneAction extends LnNamedPaneAction {

    public Pr3SelectPaneAction() {
        super(Bundle.getMessage("MenuItemPr3ModeSelect"),
                new JmriJFrameInterface(),
                Pr3SelectPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}

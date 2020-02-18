package jmri.jmrix.loconet.pr4.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Pane for downloading software updates to PRICOM products
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class Pr4SelectPane extends jmri.jmrix.loconet.pr3.swing.Pr3SelectPane {

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.pr4.swing.Pr4Select"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemPr4ModeSelect"));
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemPr4ModeSelect"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Pr4SelectPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(Pr4SelectPane.class);

}

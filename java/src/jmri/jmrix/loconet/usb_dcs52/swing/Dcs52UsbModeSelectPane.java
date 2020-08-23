package jmri.jmrix.loconet.usb_dcs52.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Pane for downloading software updates to PRICOM products
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class Dcs52UsbModeSelectPane extends jmri.jmrix.loconet.pr3.swing.Pr3SelectPane {

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.usb_dcs52.swing.Dcs52UsbModeSelect"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemUsbDcs52ModeSelect"));
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemUsbDcs52ModeSelect"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Dcs52UsbModeSelectPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(Dcs52UsbModeSelectPane.class);

}

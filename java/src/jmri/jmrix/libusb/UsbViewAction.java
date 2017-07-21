package jmri.jmrix.libusb;

import jmri.util.usb.UsbBrowserAction;

/**
 * Open a USB browser.
 *
 * @author Bob Jacobsen Copyright 2008
 * @author Randall Wood (C) 2017
 */
public class UsbViewAction extends UsbBrowserAction {

    public UsbViewAction(String s) {
        super(s);
    }

    public UsbViewAction() {
        super();
    }
<<<<<<< HEAD

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // create and display
        try {
            new ch.ntb.usb.usbView.UsbView().setVisible(true);
        } catch (java.lang.UnsatisfiedLinkError ex) {
            log.error(ex.toString());
            javax.swing.JOptionPane.showMessageDialog(null, "Unable to find the libusb-win32 package.\nFor more details on how to installed it please check http://www.jmri.org/install/USB.shtml");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(UsbViewAction.class.getName());
=======
>>>>>>> JMRI/master
}

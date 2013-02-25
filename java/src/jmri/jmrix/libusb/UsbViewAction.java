// UsbViewAction.java

package jmri.jmrix.libusb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invoke the UsbView tool from libusb-java.
 *
 * @author   Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class UsbViewAction extends javax.swing.AbstractAction {

    public UsbViewAction(String s) { 
        super(s);
    }

    public UsbViewAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrix.libusb.UsbViewActionBundle").getString("USB_Device_Viewer"));
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        // create and display
        try{
            new ch.ntb.usb.usbView.UsbView().setVisible(true);
        } catch (java.lang.UnsatisfiedLinkError ex){
            log.error(ex.toString());
            javax.swing.JOptionPane.showMessageDialog(null, "Unable to find the libusb-win32 package.\nFor more details on how to installed it please check http://www.jmri.org/install/USB.shtml");
        }
    }
    
    static Logger log = LoggerFactory.getLogger(UsbViewAction.class.getName());
}

/* @(#)UsbViewAction.java */

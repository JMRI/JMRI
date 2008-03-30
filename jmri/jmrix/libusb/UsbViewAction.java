// UsbViewAction.java

package jmri.jmrix.libusb;

/**
 * Invoke the UsbView tool from libusb-java.
 *
 * @author   Bob Jacobsen Copyright 2008
 * @version	$Revision: 1.2 $
 */
public class UsbViewAction extends javax.swing.AbstractAction {

    public UsbViewAction(String s) { 
        super(s);
    }

    public UsbViewAction() {
        this("USB Device Viewer");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        // create and display
        new ch.ntb.usb.usbView.UsbView().setVisible(true);
    }
}

/* @(#)UsbViewAction.java */

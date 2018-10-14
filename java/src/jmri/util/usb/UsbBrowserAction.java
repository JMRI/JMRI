package jmri.util.usb;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to open the UsbBrowserFrame.
 *
 * @author Randall Wood Copyright 2017
 */
public class UsbBrowserAction extends AbstractAction {

    public UsbBrowserAction(String s) {
        super(s);
    }

    public UsbBrowserAction() {
        super(Bundle.getMessage("UsbBrowserFrame.Title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new UsbBrowserFrame().setVisible(true);
    }

}

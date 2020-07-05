package jmri.jmrix.libusb;

import jmri.util.usb.UsbBrowserAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Open a USB browser.
 *
 * @author Bob Jacobsen Copyright 2008
 * @author Randall Wood (C) 2017
 */
@API(status = EXPERIMENTAL)
public class UsbViewAction extends UsbBrowserAction {

    public UsbViewAction(String s) {
        super(s);
    }

    public UsbViewAction() {
        super();
    }
}

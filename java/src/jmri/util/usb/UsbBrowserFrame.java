package jmri.util.usb;

import jmri.util.JmriJFrame;

/**
 *
 * @author rhwood
 */
public class UsbBrowserFrame extends JmriJFrame {

    public UsbBrowserFrame() {
        super(Bundle.getMessage("UsbBrowserFrame.Title"));
        super.getRootPane().setContentPane(new UsbBrowserPanel());
        super.pack();
    }
}

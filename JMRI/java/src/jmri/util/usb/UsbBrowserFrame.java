package jmri.util.usb;

import jmri.util.JmriJFrame;
import javax.swing.JPanel; 

/**
 *
 * @author rhwood
 */
public class UsbBrowserFrame extends JmriJFrame {

    public UsbBrowserFrame() {
        this(new UsbBrowserPanel());
    }

    UsbBrowserFrame(JPanel rootPanel) {
        super(Bundle.getMessage("UsbBrowserFrame.Title"));
        super.getRootPane().setContentPane(rootPanel);
        super.pack();
    }
}

package jmri.util.usb;

import jmri.util.JmriJFrame;
import javax.swing.JPanel; 

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
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

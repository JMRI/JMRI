package jmri.util.usb;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to open the UsbBrowserFrame.
 *
 * @author Randall Wood Copyright 2017
 */
@API(status = EXPERIMENTAL)
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

package jmri.jmrix.loconet.demoport;

import javax.swing.Icon;

import jmri.jmrit.swing.ToolsMenuAction;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Action to launch DemoSerialPort.
 *
 * @author Paul Bender       Copyright (C) 2003
 * @author Bob Jacobsen      Copyright (C) 2023
 * @author Daniel Bergqvist  Copyright (C) 2024
 */

// Uncomment the line below to test this class
//@org.openide.util.lookup.ServiceProvider(service = jmri.jmrit.swing.ToolsMenuAction.class)

public class DemoSerialPortAction extends JmriAbstractAction implements ToolsMenuAction {

    public DemoSerialPortAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DemoSerialPortAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public DemoSerialPortAction(String s) {
        super(s);
    }

    public DemoSerialPortAction() {
        this("Demo serial port");
    }

    @Override
    public JmriPanel makePanel() {

        // create a new panel of your specific type here
        jmri.jmrix.loconet.demoport.DemoPanel retval = new DemoPanel();

        retval.initComponents();
        return retval;
    }

}

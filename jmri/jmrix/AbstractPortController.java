// AbstractPortController.java

package jmri.jmrix;

import java.io.*;

import javax.swing.*;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance.
 * SerialPortAdapter is an Interface, and it's implementing
 * classes also inherit from various PortController types.  But we
 * want some common behaviours for those, so we put them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.2 $
 */
abstract public class AbstractPortController {

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();

    /**
     * Standard error handling for port-busy case
     */
    public String handlePortBusy(javax.comm.PortInUseException p,
                            String portName,
                            org.apache.log4j.Category log) {
				log.error(portName+" port is in use: "+p.getMessage());
                JOptionPane.showMessageDialog(null, "Port is in use",
                                                "Error", JOptionPane.ERROR_MESSAGE);
				return portName+" port is in use";
    }

}

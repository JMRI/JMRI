// Port.java
package jmri.jmrix.can.adapters.loopback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.AbstractSerialPortController;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Loopback connection to simulate a CAN link
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 * @version	$Revision$
 */
public class Port extends AbstractSerialPortController {

    public Port() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol";
        options.put(option1Name, new Option("Connection Protocol", jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
        mPort = "(None)";
    }

    public void configure() {

        // Register the CAN traffic controller being used for this connection
        this.getSystemConnectionMemo().setTrafficController(new LoopbackTrafficController());

        // do central protocol-specific configuration    
        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        this.getSystemConnectionMemo().configureManagers();

    }

    // check that this object is ready to operate
    public boolean status() {
        return true;
    }

    //////////////
    // not used //
    //////////////
    // Streams not used in simulations
    public DataInputStream getInputStream() {
        return null;
    }

    public DataOutputStream getOutputStream() {
        return null;
    }

    public String[] validBaudRates() {
        return new String[]{"None"};
    }

    public String openPort(String portName, String appName) {
        return "invalid request";
    }

    public java.util.Vector<String> getPortNames() {
        java.util.Vector<String> v = new java.util.Vector<String>();
        v.addElement("(None)");
        return v;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public CanSystemConnectionMemo getSystemConnectionMemo() {
        return (CanSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}

/* @(#)Port.java */

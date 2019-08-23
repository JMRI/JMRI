package jmri.jmrix.can.adapters.loopback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

import jmri.jmrix.AbstractSerialPortController;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Loopback connection to simulate a CAN link.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class Port extends AbstractSerialPortController {

    public Port() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionProtocol"), jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
        mPort = "(none)"; // is hidden from user in connection config UI
    }

    @Override
    public void configure() {

        // register the CAN traffic controller being used for this connection
        this.getSystemConnectionMemo().setTrafficController(new LoopbackTrafficController());

        // do central protocol-specific configuration    
        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        this.getSystemConnectionMemo().configureManagers();

    }

    // check that this object is ready to operate
    @Override
    public boolean status() {
        return true;
    }

    //////////////
    // not used //
    //////////////
    // Streams not used in simulations
    @Override
    public DataInputStream getInputStream() {
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"None"};
    } // is hidden from user in connection config UI

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{0}; // array length must match that of validBaudRates
    }

    @Override
    public String openPort(String portName, String appName) {
        return "invalid request";
    }

    @Override
    public java.util.Vector<String> getPortNames() {
        java.util.Vector<String> v = new java.util.Vector<String>();
        v.addElement(Bundle.getMessage("none"));
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

package jmri.jmrix.dccpp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a DCCpp communications port
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2004,2010
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetSimulatorPortController by Bob Jacobsen, Paul Bender
 */
public abstract class DCCppSimulatorPortController extends jmri.jmrix.AbstractSerialPortController implements DCCppPortController {

    public DCCppSimulatorPortController() {
        super(new DCCppSystemConnectionMemo());
    }

    // base class. Implementations will provide InputStream and OutputStream
    // objects to DCCppTrafficController classes, who in turn will deal in messages.    
    // returns the InputStream from the port
    @Override
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public abstract boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     */
    @Override
    public abstract boolean okToSend();

    /**
     * We need a way to say if the output buffer is empty or not
     */
    @Override
    public abstract void setOutputBufferEmpty(boolean s);

    @Override
    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return (DCCppSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}




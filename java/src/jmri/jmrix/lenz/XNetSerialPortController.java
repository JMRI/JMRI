// XNetSerialPortController.java
package jmri.jmrix.lenz;

import gnu.io.SerialPort;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010
  */
public abstract class XNetSerialPortController extends jmri.jmrix.AbstractSerialPortController implements XNetPortController {

    protected SerialPort activeSerialPort = null;

    private boolean OutputBufferEmpty = true;

    public XNetSerialPortController() {
        super(new XNetSystemConnectionMemo());
        //option2Name = "Buffer";
        //options.put(option2Name, new Option("Check Buffer : ", validOption2));
    }

    // base class. Implementations will provide InputStream and OutputStream
    // objects to XNetTrafficController classes, who in turn will deal in messages.    
    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    public abstract boolean status();

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     */
    public boolean okToSend() {
        if ((activeSerialPort.getFlowControlMode() & SerialPort.FLOWCONTROL_RTSCTS_OUT) == SerialPort.FLOWCONTROL_RTSCTS_OUT) {
            if (checkBuffer) {
                log.debug("CTS: " + activeSerialPort.isCTS() + " Buffer Empty: " + OutputBufferEmpty);
                return (activeSerialPort.isCTS() && OutputBufferEmpty);
            } else {
                log.debug("CTS: " + activeSerialPort.isCTS());
                return (activeSerialPort.isCTS());
            }
        } else {
            if (checkBuffer) {
                log.debug("Buffer Empty: " + OutputBufferEmpty);
                return (OutputBufferEmpty);
            } else {
                log.debug("No Flow Control or Buffer Check");
                return (true);
            }
        }
    }

    /**
     * we need a way to say if the output buffer is empty or full this should
     * only be set to false by external processes
     *
     */
    synchronized public void setOutputBufferEmpty(boolean s) {
        OutputBufferEmpty = s;
    }


    /* Option 2 is not currently used with RxTx 2.0.  In the past, it
     was used for the "check buffer status when sending" If this is still set        in a configuration file, we need to handle it, but we are not writing it        to new configuration files. */
    /*public String getCurrentOption2Setting() {
     if(getOptionState(option2Name)==null) return("no");
     else return getOptionState(option2Name);
     }*/
    protected String[] validOption2 = new String[]{"yes", "no"};
    private boolean checkBuffer = false;

    /* Allow derived classes to set the private checkBuffer value */
    protected void setCheckBuffer(boolean b) {
        checkBuffer = b;
    }

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSerialPortController.class.getName());

}


/* @(#)XNetSerialPortController.java */

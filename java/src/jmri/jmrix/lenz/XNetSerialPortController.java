package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;

/**
 * Abstract base for classes representing an XNet communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2004,2010
  */
public abstract class XNetSerialPortController extends jmri.jmrix.AbstractSerialPortController implements XNetPortController {

    protected SerialPort activeSerialPort = null;

    private boolean outputBufferEmpty = true;

    private boolean timeSlot = true;

    public XNetSerialPortController() {
        super(new XNetSystemConnectionMemo());
        //option2Name = "Buffer";
        //options.put(option2Name, new Option("Check Buffer : ", validOption2));
    }

    public XNetSerialPortController(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    // base class. Implementations will provide InputStream and OutputStream
    // objects to XNetTrafficController classes, who in turn will deal in messages.    
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
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     */
    @Override
    public boolean okToSend() {
        if ((activeSerialPort.getFlowControlMode() & SerialPort.FLOWCONTROL_RTSCTS_OUT) == SerialPort.FLOWCONTROL_RTSCTS_OUT) {
            if (checkBuffer) {
                log.debug("CTS: " + activeSerialPort.isCTS() + " Buffer Empty: " + outputBufferEmpty);
                return (activeSerialPort.isCTS() && outputBufferEmpty);
            } else {
                log.debug("CTS: " + activeSerialPort.isCTS());
                return (activeSerialPort.isCTS());
            }
        } else {
            if (checkBuffer) {
                log.debug("Buffer Empty: {}", outputBufferEmpty);
                return (outputBufferEmpty && hasTimeSlot() );
            } else {
                log.debug("No Flow Control or Buffer Check");
                return (hasTimeSlot());
            }
        }
    }

    /**
     * Indicate whether the Command Station is currently providing a timeslot to this
     * port controller.
     *
     * @return true if the command station is currently providing a timeslot.
     */
    @Override
    public boolean hasTimeSlot(){
        return timeSlot;
    }
    
    /**
     * Set a variable indicating whether or not the command station is
     * providing a timeslot.
     * <p>
     * This method should be called with the paramter set to false if
     * a "Command Station No Longer Providing a timeslot for communications"
     * (01 05 04) is received.
     * <p>
     * This method should be called with the parameter set to true if
     * a "Command Station is providing a timeslot for communications again."
     * (01 07 06) is received.
     *
     * @param timeslot true if a timeslot is being sent, false otherwise.
     */
    @Override
    public void setTimeSlot(boolean timeslot){
       timeSlot = timeslot;
    }    


    /**
     * We need a way to say if the output buffer is empty or full.
     * <p>
     * This should only be set to false by external processes.
     */
    @Override
    synchronized public void setOutputBufferEmpty(boolean s) {
        outputBufferEmpty = s;
    }


    /* Option 2 is not currently used with RxTx 2.0.  In the past, it
     was used for the "check buffer status when sending" If this is still set
     in a configuration file, we need to handle it, but we are not writing it
     to new configuration files. */
    /*public String getCurrentOption2Setting() {
     if(getOptionState(option2Name)==null) return("no");
     else return getOptionState(option2Name);
     }*/
    protected String[] validOption2 = new String[]{"yes", "no"};
    private boolean checkBuffer = false;

    /**
     * Allow derived classes to set the private checkBuffer value
     */
    protected void setCheckBuffer(boolean b) {
        checkBuffer = b;
    }

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSerialPortController.class);

}

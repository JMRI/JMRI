package jmri.jmrix.lenz;

import jmri.jmrix.AbstractThrottle;

//import jmri.jmrix.lenz.XNetListener;
//import jmri.jmrix.lenz.XNetTrafficController;

/**
 * An implementation of DccThrottle with code specific to a 
 * XpressnetNet connection.
 */
public class XNetThrottle extends AbstractThrottle implements XNetListener
{
    private float speedSetting;
    private float speedIncrement;
    private int address;
    private boolean isForward;
    private boolean f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12;

    /**
     * Constructor
     */
    public XNetThrottle()
    {
       super();
       XNetTrafficController.instance().addXNetListener(~0, this);
       log.error("XnetThrottle constructor");
    }

    /**
     * Send the XpressNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
    {
       XNetMessage msg=new XNetMessage(5);
       msg.setOpCode(0xE4);   // hex E4 is the Opcode for setting 
                                    // functions
       msg.setElement(1,0x20);// hex 20 is the subset of comand E4 
                                    // for setting Functions F0-F4
       msg.setElement(2,0x00);// For now, the upper 8 bits of the 
                                    // address is going to be 00 (i.e. we 
                                    // can only address addresses 0-99
       msg.setElement(3,this.getDccAddress()); // set the DCC address 
                                    // to the third element

       msg.setElement(4,10);// for now, we just turn on FO
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
    }

    /**
     * Send the XpressNet message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2()
    {
    }

    /**
     * Send the XpressNet message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3()
    {
    }

   /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting()
    {
        return speedSetting;
    }

    public void setSpeedSetting(float speed)
    {
        this.speedSetting = speed;
    }

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward()
    {
        return isForward;
    }

    public void setIsForward(boolean forward)
    {
        isForward = forward;
        sendFunctionGroup1();
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0()
    {
        return f0;
    }

    public void setF0(boolean f0)
    {
        this.f0 = f0;
        sendFunctionGroup1();
    }

    public boolean getF1()
    {
        return f1;
    }

    public void setF1(boolean f1)
    {
        this.f1 = f1;
        sendFunctionGroup1();

    }

    public boolean getF2()
    {
        return f2;
    }

    public void setF2(boolean f2)
    {
        this.f2 = f2;
        sendFunctionGroup1();
    }

    public boolean getF3()
    {
        return f3;
    }

    public void setF3(boolean f3)
    {
        this.f3 = f3;
        sendFunctionGroup1();
    }

    public boolean getF4()
    {
        return f4;
    }

    public void setF4(boolean f4)
    {
        this.f4 = f4;
        sendFunctionGroup1();
    }


    public boolean getF5()
    {
        return f5;
    }

    public void setF5(boolean f5)
    {
        this.f5 = f5;
        sendFunctionGroup2();
    }

    public boolean getF6()
    {
        return f6;
    }

    public void setF6(boolean f6)
    {
        this.f6 = f6;
        sendFunctionGroup2();
    }


    public boolean getF7()
    {
        return f7;
    }

    public void setF7(boolean f7)
    {
        this.f7 = f7;
        sendFunctionGroup2();

    }


    public boolean getF8()
    {
        return f8;
    }

    public void setF8(boolean f8)
    {
        this.f8 = f8;
        sendFunctionGroup2();
    }

    public boolean getF9()
    {
        return f9;
    }

    public void setF9(boolean f9)
    {
        this.f9 = f9;
        sendFunctionGroup3();
    }

    public boolean getF10()
    {
        return f10;
    }

    public void setF10(boolean f10)
    {
        this.f10 = f10;
        sendFunctionGroup3();
    }

    public boolean getF11()
    {
        return f11;
    }

    public void setF11(boolean f11)
    {
        this.f11 = f11;
        sendFunctionGroup3();
    }

    public boolean getF12()
    {
        return f12;
    }

    public void setF12(boolean f12)
    {
        this.f12 = f12;
        sendFunctionGroup3();
    }


    /**
     * Locomotive identification.  The exact format is defined by the
     * specific implementation, but its intended that this is a user-specified
     * name like "UP 777", or whatever convention the user wants to employ.
     *
     * This is an unbound parameter.
     */
    public String getLocoIdentification()
    {
        return "";
    }


    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, but for DCC systems it is intended that this
     * will be the DCC address in the form "nnnn" (extended) vs "nnn" or "nn" (short).
     * Non-DCC systems may use a different form.
     *
     * This is an unbound parameter.
     */
    public String getLocoAddress()
    {
        return "";
    }


    // register for notification if any of the properties change
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p)
    {
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p)
    {
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when
     * it's the last user.
     */
    public void dispose()
    {
    }

    public int setDccAddress(int newaddress)
    {
        address=newaddress;
        return address;
    }

    public int getDccAddress()
    {
        return address;
    }

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement()
    {
        return speedIncrement;
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //          public void firePropertyChange(String propertyName,
    //                                                                         
    //                                                                         
    // _once_ if anything has changed state (or set the commanded state directly
    public void message(XNetMessage l) {
        // check validity & addressing
        if (XNetTrafficController.instance()
            .getCommandStation()
            .getThrottleMsgAddr(l) != address) return;
        // is for this object, parse message type
        log.error("message function invoked, but not yet prepared");
    }


    // information on consisting  (how do we set consisting?)

    // register for notification

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottle.class.getName());
}

// XNetSensor.java

package jmri.jmrix.lenz;

import jmri.AbstractSensor;
import jmri.Sensor;

/**
 * Extend jmri.AbstractSensor for XPressNet layouts.
 * <P>
 * @author			Paul Bender Copyright (C) 2003
 * @version         $Revision: 1.1 $
 */
public class XNetSensor extends AbstractSensor implements XNetListener {

    private int address;

    public XNetSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public XNetSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
        address = Integer.parseInt(id.substring(2,id.length()));
        if (log.isDebugEnabled()) log.debug("create address " + address);

        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
    }

    /**
     * request an update on status by sending an XPressNet message
     */
    public void requestUpdateFromLayout() {
       // To do this, we send an XpressNet Accessory Decoder Information 
       // Request.
       // This works for Feedback modules and turnouts with feedback.
       XNetMessage msg = XNetTrafficController.instance()
                                              .getCommandStation()
                                              .getFeedbackRequestMsg(address,
                                                                       true);
       XNetTrafficController.instance().sendXNetMessage(msg, this);
       msg = XNetTrafficController.instance().getCommandStation()
                                             .getFeedbackRequestMsg(address,
                                                                       false);
       XNetTrafficController.instance().sendXNetMessage(msg, this);
    }

    /**
     * User request to set the state.
     * @param s
     * @throws JmriException
     */
    public void setKnownState(int s) throws jmri.JmriException {
    }

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call
     *      public void firePropertyChange(String propertyName,
     *      					Object oldValue,
     *                                          Object newValue)
     * _once_ if anything has changed state (or set the commanded state directly)
     * @param l
     */
    public void message(XNetMessage l) {
        return;
    }

    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetSensor.class.getName());

}


/* @(#)XNetSensor.java */

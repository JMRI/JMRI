package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The XNetTimeSlotListener listens for two messages from the computer interface:
 * <ol>
 * <li>"Command Station No Longer Providing a timeslot for communications" (01 05 04)</li>
 * <li>"Command Station is providing a timeslot for communications again." (01 07 06)</li>
 * </ol>
 * <p>
 * when the first message is received, the associated port controller's 
 * setTimeSlot methodis called with a "false" parameter.  When the second 
 * is true, it is called with a "true paramter.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetTimeSlotListener implements XNetListener {

    private XNetPortController port = null;

    public XNetTimeSlotListener(XNetPortController p){
       port = p;
       log.debug("Time Slot Listener created");
    }

    /**
     * Member function that will be invoked by an XNetInterface implementation to
     * forward an XNet message from the layout.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(XNetReply msg){
        log.debug("Time Slot Listener received {}",msg);
        if(msg.isTimeSlotErrorMessage()){
           if(msg.isTimeSlotRevoked()){
              log.debug("Time Slot Revoked Received");
              port.setTimeSlot(false);
           } else if(msg.isTimeSlotRestored()) {
              log.debug("Time Slot Restored Received");
              port.setTimeSlot(true);
           } else {
              log.debug("Message Sent while we had no timeslot");
              port.setTimeSlot(false);
           }
        }
    }

    /**
     * Member function that will be invoked by an XNetInterface implementation to
     * forward an XNet message sent to the layout. Normally, this function will
     * do nothing.
     *
     * @param msg The received XNet message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(XNetMessage msg){
       // do nothing
    }

    /**
     * Member function invoked by an XNetInterface implementation to notify a
     * sender that an outgoing message timed out and was dropped from the
     * queue.
     */
    @Override
    public void notifyTimeout(XNetMessage msg){
       // do nothing
    }

    private final static Logger log = LoggerFactory.getLogger(XNetTimeSlotListener.class);

}

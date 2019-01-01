package jmri.jmrix.roco.z21;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.RailCom;
import jmri.RailComManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z21CanReporter implements the Reporter Manager interface
 * for Can connected reporters on Roco Z21 systems.
 * <p>
 * Reports from this reporter are of the type jmri.RailCom.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21CanReporter extends jmri.implementation.AbstractRailComReporter implements Z21Listener {

    private Z21SystemConnectionMemo _memo = null;

    private int networkID; // CAN network ID associated with this reporter's module.
    private int moduleAddress; // User assigned address associated with this reporter's module.
    private int port; // module port (0-7) associated with this reporter.

    /**  
     * Create a new Z21CanReporter.
     *
     * @param systemName the system name of the new reporter.
     * @param userName the user name of the new reporter.
     * @param memo an instance of Z21SystemConnectionMemo this reporter 
     *             is associated with.
     *
     */
    public Z21CanReporter(String systemName,String userName,Z21SystemConnectionMemo memo){
        super(systemName,userName);
        _memo = memo;
        _memo.getTrafficController().addz21Listener(this);
        //Address format passed is in the form of moduleAddress:pin 
        int seperator = systemName.indexOf(":");
        int start = _memo.getSystemPrefix().length() + 1;
           try {
              moduleAddress = (Integer.parseInt(systemName.substring(start,seperator)));
              port = (Integer.parseInt(systemName.substring(seperator + 1)));
           } catch (NumberFormatException ex) {
              log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
              throw new IllegalArgumentException("requires mm:pp format address.");
           }
        // request an update from the layout.
       requestUpdateFromLayout();
    }

    /**
     *     request an update from the layout.
     */
    private void requestUpdateFromLayout(){
       //_memo.getTrafficController().sendz21Message(Z21Message.getLanRailComGetDataRequestMessage(),this);
    }

    // the Z21 Listener interface

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message from the layout.
     *
     * @param msg The received z21 reply. Note that this same object may be
     * presented to multiple users. It should not be modified here.
     */
    @Override
    public void reply(Z21Reply msg){
         // for incoming messages all the reporter cares about is
         // LAN_CAN_DETECTOR messages.
         if(msg.isCanDetectorMessage()){
            int netID = ( msg.getElement(4)&0xFF) + ((msg.getElement(5)&0xFF) << 8);
            int address = ( msg.getElement(6)&0xFF) + ((msg.getElement(7)&0xFF) << 8);
            if(address != moduleAddress ) {
                return; // not our messge.
            }
            int msgPort = ( msg.getElement(8) & 0xFF);
            if( msgPort != port ) {
                return; // not our messge.
            }
            int type = ( msg.getElement(9) & 0xFF);
            int value1 = (msg.getElement(10)&0xFF) + ((msg.getElement(11)&0xFF) << 8);
            int value2 = (msg.getElement(12)&0xFF) + ((msg.getElement(13)&0xFF) << 8);
            // get the first locomotive address from the message.
            DccLocoAddress l = msg.getCanDetectorLocoAddress(value1);
            if(l!=null) {
               // see if there is a tag for this address.
               RailCom tag = InstanceManager.getDefault(RailComManager.class).provideIdTag("" + l.getNumber());
               tag.setAddressType(l.isLongAddress()?RailCom.LONG_ADDRESS:RailCom.SHORT_ADDRESS);
               notify(tag);
               // get the second locomotive address from the message.
               DccLocoAddress l2 = msg.getCanDetectorLocoAddress(value2);
               if(l2!=null) {
                  // see if there is a tag for this address.
                  RailCom tag2 = InstanceManager.getDefault(RailComManager.class).provideIdTag("" + l2.getNumber());
                  tag2.setAddressType(l2.isLongAddress()?RailCom.LONG_ADDRESS:RailCom.SHORT_ADDRESS);
                  notify(tag2);
                }
             } else {
                // todo: check which address in the list this is. only
                // say no report if the first entry is the last entry.
                notify(null); // clear the current report if no tags.
             }
         }
    }

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message sent to the layout. Normally, this function will do
     * nothing.
     *
     * @param msg The received z21 message. Note that this same object may be
     * presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(Z21Message msg){
         // we don't need to handle outgoing messages, so just ignore them.
    }

    @Override
    public void dispose(){
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(Z21CanReporter.class);

}

package jmri.jmrix.roco.z21;

import jmri.Reporter;
import jmri.RailCom; 
import jmri.IdTag; 
import jmri.RailComManager;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.DccLocoAddress;

/**
 * Z21Reporter implements the Reporter Manager interface
 * for Roco Z21 systems.
 * <P>
 * Reports from this reporter are of the type jmri.RailCom.
 * <P>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21Reporter extends jmri.implementation.AbstractRailComReporter implements Z21Listener {

    private Z21SystemConnectionMemo _memo = null;

    /**  
     * Create a new Z21Reporter
     * @param systemName the system name of the new reporter.
     * @param userName the user name of the new reporter.
     * @param memo an instance of Z21SystemConnectionMemo this manager 
     *             is associated with.
     *
     */
    public Z21Reporter(String systemName,String userName,Z21SystemConnectionMemo memo){
        super(systemName,userName);
        _memo = memo;
        _memo.getTrafficController().addz21Listener(this);
        // request an update from the layout.
       _memo.getTrafficController().sendz21Message(Z21Message.getLanRailComGetDataRequestMessage(),this);
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
         // LAN_RAILCOM_DATACHANGED messages.
         if(msg.isRailComDataChangedMessage()){
             // find out how many RailCom Transmitters the command
             // station is telling us about (there is a maximum of 19).
             int tags = msg.getNumRailComDataEntries();
             for(int i=0;i<tags;i++){
                 // get the locomotive address from the message.
                 DccLocoAddress l = msg.getRailComLocoAddress(i);
                 // see if there is a tag for this address.
                 IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag("" + l.getNumber());
                 // set the tag report.
                 notify(tag);
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



    public void dispose(){
        super.dispose();
    }
}

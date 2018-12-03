package jmri.jmrix.roco.z21;

import jmri.InstanceManager;
import jmri.RailComManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Z21ReporterManager implements the Reporter Manager interface
 * for Roco Z21 systems.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21ReporterManager extends jmri.managers.AbstractReporterManager implements Z21Listener {

    private Z21SystemConnectionMemo _memo = null;

    /**
     * Create a new Z21ReporterManager
     * @param memo an instance of Z21SystemConnectionMemo this manager
     *             is associated with.
     */
    public Z21ReporterManager(Z21SystemConnectionMemo memo){
        _memo = memo;
        try{
           InstanceManager.getDefault(RailComManager.class);
        } catch(NullPointerException npe) {
              // there is no RailComManager, so create a new one
              InstanceManager.setDefault(RailComManager.class,
                                     new jmri.managers.DefaultRailComManager());
        }
        _memo.getTrafficController().addz21Listener(this);
        // make sure we are going to get railcom data from the command station
        // set the broadcast flags so we get messages we may want to hear
        _memo.getRocoZ21CommandStation().setRailComMessagesFlag(true);
        // and forward the flags to the command station.
        _memo.getTrafficController().sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
              _memo.getRocoZ21CommandStation().getZ21BroadcastFlags()),null);
        // And then send a message requesting an update from the hardware.
        // This is required because the RailCom data currently requires polling.
        _memo.getTrafficController().sendz21Message(Z21Message.getLanRailComGetDataRequestMessage(),this);
    }

    @Override
    public String getSystemPrefix(){
        return _memo.getSystemPrefix();
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName){
        if(!systemName.matches(getSystemPrefix() + typeLetter() + "[" + 1 + "]")) {
            log.warn("Invalid Reporter name: " + systemName + " - only one reporter supported ");
            throw new IllegalArgumentException("Invalid Reporter name: " + systemName + " - only one reporter supported ");
        }
        // Create and register the reporter
        Reporter r = new Z21Reporter(systemName,userName,_memo);
        register(r);
        return r;
    }

    @Override
    public void dispose(){
        super.dispose();
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
            log.debug("Received RailComDatachanged message");
            Z21Reporter r = (Z21Reporter) getBySystemName(getSystemPrefix()+typeLetter()+1); // there is only one turnout.
           if ( null == r ) {
              log.debug("Creating reporter {}",getSystemPrefix()+typeLetter()+1);
              // need to create a new one, and send the message on 
              // to the newly created object.
              ((Z21Reporter)provideReporter(getSystemPrefix()+typeLetter()+1)).reply(msg);
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

    private static final Logger log = LoggerFactory.getLogger(Z21ReporterManager.class);

}

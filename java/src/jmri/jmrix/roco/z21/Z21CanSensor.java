package jmri.jmrix.roco.z21;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z21CanSensor implements the Sensor interface
 * for Can connected sensors on Roco Z21 systems.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21CanSensor extends jmri.implementation.AbstractSensor implements Z21Listener {

    private Z21SystemConnectionMemo _memo = null;

    private int networkID=0; // CAN network ID associated with this reporter's module.
    private int moduleAddress=-1; // User assigned address associated with this reporter's module.
    private int port; // module port (0-7) associated with this reporter.

    /**  
     * Create a new Z21CanSensor.
     *
     * @param systemName the system name of the new reporter.
     * @param userName the user name of the new reporter.
     * @param memo an instance of Z21SystemConnectionMemo this reporter 
     *             is associated with.
     *
     */
    public Z21CanSensor(String systemName,String userName,Z21SystemConnectionMemo memo){
        super(systemName,userName);
        _memo = memo;
        // register for messages
        _memo.getTrafficController().addz21Listener(this);
        //Address format passed is in the form of moduleAddress:pin 
        try {
            setIdentifiersFromSystemName(systemName);
        } catch (NumberFormatException ex) {
           log.debug("Unable to convert {} into the cab and input format of nn:xx",systemName);
           throw new IllegalArgumentException("requires mm:pp format address.");
        }
    }

    private void setIdentifiersFromSystemName(String systemName) {
        String moduleAddressText = Z21CanBusAddress.getEncoderAddressString(systemName, _memo.getSystemPrefix());
        try {
            moduleAddress = Integer.parseInt(moduleAddressText);
        } catch (NumberFormatException ex) {
            // didn't parse as a decimal, check to see if network ID
            // was used instead.
            networkID = Integer.parseInt(moduleAddressText, 16);
        }
        port = Z21CanBusAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
    }

    /**
     *     request an update from the layout.
     */
    @Override
    public void requestUpdateFromLayout(){
       if(networkID==0){
          return; // no networkID has been set yet.
       }
       _memo.getTrafficController().sendz21Message(Z21Message.getLanCanDetector(networkID),this);
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
         if(msg.isCanSensorMessage()){
            int netID = ( msg.getElement(4)&0xFF) + ((msg.getElement(5)&0xFF) << 8);
            int address = ( msg.getElement(6)&0xFF) + ((msg.getElement(7)&0xFF) << 8);
            int msgPort = ( msg.getElement(8) & 0xFF);
            if(!messageForSensor(address,netID,msgPort)){
                return;
            }
            // status message, use to set state.
            int value1 = (msg.getElement(10)&0xFF) + ((msg.getElement(11)&0xFF) << 8);
            log.debug("value {}",value1);
            if(value1 == 0x0000) {
                log.debug("Free without voltage");
                setOwnState(Sensor.INACTIVE);
            } else if(value1 == 0x0100) {
                log.debug("Free with voltage");
                setOwnState(Sensor.INACTIVE);
            } else if(value1 == 0x1000) {
                log.debug("Busy without voltage");
                setOwnState(Sensor.ACTIVE);
            } else if(value1 == 0x1100) {
                log.debug("Busy with voltage");
                setOwnState(Sensor.ACTIVE);
            } else if(value1 == 0x1201) {
                log.debug("Busy Overload 1");
                setOwnState(Sensor.ACTIVE);
            } else if(value1 == 0x1202) {
                log.debug("Busy Overload 2");
                setOwnState(Sensor.ACTIVE);
            } else if(value1 == 0x1203) {
                log.debug("Busy Overload 3");
                setOwnState(Sensor.ACTIVE);
            }
         }
    }

    private boolean messageForSensor(int address,int netId,int msgPort){
        return (address == moduleAddress || netId == networkID) &&  msgPort == port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(Z21Message msg){
         // we don't need to handle outgoing messages, so just ignore them.
    }

    @Override
    public void dispose(){
        _memo.getTrafficController().removez21Listener(this);
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(Z21CanSensor.class);

}

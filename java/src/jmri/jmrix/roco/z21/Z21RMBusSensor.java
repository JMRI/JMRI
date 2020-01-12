package jmri.jmrix.roco.z21;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for RocoMotion (RM) bus sensors on
 * the Roco Z21.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21RMBusSensor extends AbstractSensor implements Z21Listener {

    private boolean statusRequested = false;

    private int address;
    private int moduleAddress; /* The result of integer division of the 
     ( sensor address - 1) by 8 */

    private int bit;   /* a bitmask for the bit of this sensor */

    private String systemName;

    protected Z21TrafficController tc = null;

    public Z21RMBusSensor(String systemName, String userName, Z21TrafficController controller, String prefix) {
        super(systemName, userName);
        tc = controller;
        init(systemName, prefix);
    }

    public Z21RMBusSensor(String systemName, Z21TrafficController controller, String prefix) {
        super(systemName);
        tc = controller;
        init(systemName, prefix);
    }

    /**
     * Common initialization for all constructors.
     */
    private void init(String id, String prefix) {
        // store address
        systemName = id;
        address = Z21RMBusAddress.getBitFromSystemName(systemName, prefix);
        // calculate the module address and the bit to examine
        moduleAddress = ((address-1)/ 8);
        int bitnumber = (address-1) % 8;
        switch(bitnumber) {
           case 0:
               bit = 0x01;
               break;
           case 1:
               bit = 0x02;
               break;
           case 2:
               bit = 0x04;
               break;
           case 3:
               bit = 0x08;
               break;
           case 4:
               bit = 0x10;
               break;
           case 5:
               bit = 0x20;
               break;
           case 6:
               bit = 0x40;
               break;
           case 7:
               bit = 0x80;
               break;
           default:
           // no default action, we have exhausted the possibilities.
        }
        log.debug("Created Sensor {} (Module Address {},  contact {})",
                    systemName, moduleAddress, bitnumber);
        tc.addz21Listener(this);
        // Finally, request the current state from the layout.
        requestUpdateFromLayout();
    }

    /**
     * Request an update on status by sending an Z21 message.
     */
    @Override
    public void requestUpdateFromLayout() {
        Z21Message msg = Z21Message.getLanRMBusGetDataRequestMessage(moduleAddress<=10?0:1); // only two possiblities allowed.
        synchronized (this) {
            statusRequested = true;
        }
        tc.sendz21Message(msg, this);
    }

    /**
     * initmessage is a package protected class which allows the Manger to send
     * a feedback message at initialization without changing the state of the
     * sensor with respect to whether or not a feedback request was sent. This
     * is used only when the sensor is created by on layout feedback.
     */
    synchronized void initmessage(Z21Reply l) {
        boolean oldState = statusRequested;
        reply(l);
        statusRequested = oldState;
    }

    @Override
    public synchronized void reply(Z21Reply l) {
        log.debug("received message: {}",l);
        if (l.isRMBusDataChangedReply()) {
           if (((l.getElement((moduleAddress<10?5+moduleAddress:5+(moduleAddress-10))) & bit) != 0) ^ _inverted) {
             setOwnState(Sensor.ACTIVE);
           } else {
             setOwnState(Sensor.INACTIVE);
           }
        }
        return;
    }

    /**
     * Listen for the messages to the Z21.
     */
    @Override
    public void message(Z21Message l) {
    }

    @Override
    public void dispose() {
        tc.removez21Listener(this);
        super.dispose();
    }

    /**
     * Package protected routine to get the Sensor Number.
     */
    int getNumber() {
        return address;
    }

    /**
     * Package protected routine to get the Sensor Base Address.
     */
    int getModuleAddress() {
        return moduleAddress;
    }

    private final static Logger log = LoggerFactory.getLogger(Z21RMBusSensor.class);

}

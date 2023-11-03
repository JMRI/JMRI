package jmri.jmrix.bidib;

import jmri.NamedBean;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.messages.enums.OccupationState;
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for BiDiB systems
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Eckart Meyer Copyright (C) 2019-2023
 */

public class BiDiBSensor extends AbstractSensor implements BiDiBNamedBeanInterface {

    private BiDiBAddress addr;
    private final char typeLetter;
    private BiDiBTrafficController tc = null;
    //MessageListener messageListener = null;
    private BiDiBOutputMessageHandler messageHandler = null;

    // for LC Input Sensors
    LcOutputType lcType; //cached type from portConfigX or fixed drin type based address
    
    /**
     * Create a Sensor object from system name.
     *
     * @param systemName name of added Sensor
     * @param mgr Sensor Manager, we get the memo object and the type letter (S) from the manager
     */
    public BiDiBSensor(String systemName, BiDiBSensorManager mgr) {
        super(systemName);
        tc = mgr.getMemo().getBiDiBTrafficController();
        log.debug("New Sensor: {}", systemName);
        addr = new BiDiBAddress(systemName, mgr.typeLetter(), mgr.getMemo());
        log.info("New SENSOR created: {} -> {}", systemName, addr);
        typeLetter = mgr.typeLetter();
        
        createSensorListener();
        
        messageHandler.sendQueryConfig();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBAddress getAddr() {
        return addr;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void finishLoad() {
        messageHandler.sendQuery();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeNew() {
        //create a new BiDiBAddress
        addr = new BiDiBAddress(getSystemName(), typeLetter, tc.getSystemConnectionMemo());
        if (addr.isValid()) {
            log.info("new sensor address created: {} -> {}", getSystemName(), addr);
            if (addr.isPortAddr()) {
                messageHandler.sendQueryConfig();
                messageHandler.waitQueryConfig();
            }
            if (!addr.isFeedbackAddr()) {
                // sensor is not a feedback (BiDiB BM), may be a port input - feedback will be queried in a bulk reqeust from sensor manager
                messageHandler.sendQuery();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeLost() {
        setOwnState(NamedBean.UNKNOWN);
    }

    /**
     * Request an update on status.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (addr.isValid()) {
            log.info("Query sensor status from BiDiB: addr: {}", addr);
            messageHandler.sendQuery();
        }
    }
    
    /**
     * Dispose of the sensor object.
     * 
     * Remove the Message Handler for this sensor object
     */
    @Override
    public void dispose() {
        if (messageHandler != null) {
            tc.removeMessageListener(messageHandler);        
            messageHandler = null;
        }
        super.dispose();
    }

    
    private void createSensorListener() {
        // create message listener
        messageHandler = new BiDiBOutputMessageHandler(this, "SENSOR", tc) {

            @Override
            public void occupation(byte[] address, int messageNum, int detectorNumber, OccupationState occupationState, Integer timestamp) {
                //log.trace("occupation: node UID: {}, node addr: {}, address: {}, detectorNumber: {}, occ state: {}, timestamp: {}", addr.getNodeUID(), addr.getNodeAddr(), address, detectorNumber, occupationState, timestamp);
                if (NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  !addr.isPortAddr()  &&  addr.getAddr() == detectorNumber) {
                    log.info("SENSOR occupation was signalled, state: {}, BM Number: {}, node: {}", occupationState, detectorNumber, addr);
                    if (occupationState == OccupationState.OCCUPIED) {
                        setOwnState(Sensor.ACTIVE);
                    }
                    else {
                        setOwnState(Sensor.INACTIVE);
                    }
                }
            }
            @Override
            public void occupancyMultiple(byte[] address, int messageNum, int baseAddress, int detectorCount, byte[] detectorData) {
                //log.trace("occupation: node UID: {}, node addr: {}, address: {}, baseAddress: {}, detectorCount: {}, occ states: {}, timestamp: {}", addr.getNodeUID(), addr.getNodeAddr(), address, baseAddress, detectorCount, ByteUtils.bytesToHex(detectorData));
                if (NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  !addr.isPortAddr()  &&  addr.getAddr() >= baseAddress  &&  addr.getAddr() < (baseAddress + detectorCount)) {
                    // TODO: This is very inefficent, since this function is called for each sensor! We should place the listener at a more central instance like the sensor manager
                    // our address is in the data bytes. Check which byte and then check, if the correspondent bit is set.
                    //log.trace("multiple occupation was signalled, states: {}, BM base Number: {}, BM count: {}, node: {}", ByteUtils.bytesToHex(detectorData), baseAddress, detectorCount, addr);
                    int relAddr = addr.getAddr() - baseAddress;
                    byte b = detectorData[ relAddr / 8];
                    boolean isOccupied = (b & (1 << (relAddr % 8))) != 0;
                    log.info("SENSOR multi occupation was signalled, state: {}, BM addr: {}, node: {}", isOccupied ? "OCCUPIED" : "FREE", addr.getAddr(), addr);
                    if (isOccupied) {
                        setOwnState(Sensor.ACTIVE);
                    }
                    else {
                        setOwnState(Sensor.INACTIVE);
                    }
                }
            }
            @Override
            public void newOutputState(int state) {
                int newState = (state == 0) ? Sensor.INACTIVE : Sensor.ACTIVE;
                log.debug("SENSOR new state: {}", newState);
                setOwnState(newState);
            }
            @Override
            public void outputWait(int time) {
                log.debug("SENSOR wait: {}", time);
            }
            @Override
            public void errorState(int err) {
                log.warn("SENSOR error: {} addr: {}", err, addr);
                setOwnState(INCONSISTENT);
            }
        };
        tc.addMessageListener(messageHandler);
    }
    
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(BiDiBSensor.class);

}

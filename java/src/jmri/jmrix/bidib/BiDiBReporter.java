package jmri.jmrix.bidib;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Collections;

import jmri.CollectingReporter;
import jmri.InstanceManager;
import jmri.RailCom;
import jmri.RailComManager;

import org.bidib.jbidibc.messages.AddressData;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.enums.OccupationState;
import org.bidib.jbidibc.messages.utils.NodeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Reporter Manager interface
 * for BiDiB railcom feedback.
 * <p>
 * Reports from this reporter are of the type jmri.RailCom.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Eckart Meyer Copyright (C) 2019-2025
 * 
 * based on jmri.jmrix.z21.Z21reporter and others
 */
public class BiDiBReporter extends jmri.implementation.AbstractRailComReporter implements BiDiBNamedBeanInterface, CollectingReporter {

    BiDiBAddress addr;
    private final char typeLetter;
    private BiDiBTrafficController tc = null;
    MessageListener messageListener = null;
    private final Set<Object> entrySet = Collections.synchronizedSet(new HashSet<>());
    
    /**  
     * Create a reporter instance. 
     * 
     * @param systemName name to be created
     * @param mgr Reporter Manager, we get the memo object and the type letter (R) from the manager
     */
    public BiDiBReporter(String systemName, BiDiBReporterManager mgr) {
        super(systemName);
        tc = mgr.getMemo().getBiDiBTrafficController();
        log.debug("New Reporter: {}", systemName);
        addr = new BiDiBAddress(systemName, mgr.typeLetter(), mgr.getMemo());
        log.info("New REPORTER created: {} -> {}", systemName, addr);
        typeLetter = mgr.typeLetter();
        
        createReporterListener();
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
    public void nodeNew() {
        //create a new BiDiBAddress
        addr = new BiDiBAddress(getSystemName(), typeLetter, tc.getSystemConnectionMemo());
        if (addr.isValid()) {
            log.info("new reporter address created: {} -> {}", getSystemName(), addr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeLost() {
        notify_loco(null);
    }

    /**
     * Notify loco address
     * 
     * @param tag found tag
     */
    public void notify_loco(RailCom tag) {
        //log.trace("tag: {}", tag);
        super.notify(tag);
    }

    private void createReporterListener() {
        // create message listener for RailCom messages
        messageListener = new DefaultMessageListener() {
            @Override
            public void address(byte[] address, int messageNum, int detectorNumber, List<AddressData> addressData) {
                //log.trace("address: node UID: {}, node addr: {}, address: {}, detectorNumber: {}, addressData: {}", addr.getNodeUID(), addr.getNodeAddr(), address, detectorNumber, addressData);
                if (NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.getAddr() == detectorNumber) {
                    log.info("REPORTER address was signalled, locos: {}, BM Number: {}, node: {}", addressData, detectorNumber, addr);
                    synchronized(entrySet) {
                        entrySet.clear();
                        if (addressData.size() > 0) {
                            for (AddressData l : addressData) {
                                log.trace("loco addr: {}", l);
                                if (l.getAddress() > 0) {
                                    RailCom tag = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("" + l.getAddress());
                                    //tag.setActualSpeed(msg.getRailComSpeed(i));   
                                    entrySet.add(tag);
                                    notify_loco(tag);
                                }
                                else {
                                    notify_loco(null);
                                }
                            }
                        }
                        else {
                            notify_loco(null);
                        }
                    }
                }
            }
            // occupation free is catched here to report the loco has left - obviusly an "address" message is not sent then
            @Override
            public void occupation(byte[] address, int messageNum, int detectorNumber, OccupationState occupationState, Integer timestamp) {
                //log.trace("occupation: node UID: {}, node addr: {}, address: {}, detectorNumber: {}, occ state: {}, timestamp: {}", addr.getNodeUID(), addr.getNodeAddr(), address, detectorNumber, occupationState, timestamp);
                if (NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.getAddr() == detectorNumber) {
                    if (occupationState == OccupationState.FREE) {
                        log.debug("REPORTER occupation free signalled, state: {}, BM Number: {}, node: {}", occupationState, detectorNumber, addr);
                        synchronized(entrySet) {
                            entrySet.clear();
                        }
                        notify_loco(null);
                    }
                }
            }
            @Override
            public void occupancyMultiple(byte[] address, int messageNum, int baseAddress, int detectorCount, byte[] detectorData) {
                log.trace("occupation: node UID: {}, node addr: {}, address: {}, baseAddress: {}, detectorCount: {}, occ states: {}", 
                        addr.getNodeUID(), addr.getNodeAddr(), address, baseAddress, 
                        detectorCount, detectorData);
                if (NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  !addr.isPortAddr()  &&  addr.getAddr() >= baseAddress  &&  addr.getAddr() < (baseAddress + detectorCount)) {
                    // TODO: This is very inefficent, since this function is called for each sensor! We should place the listener at a more central instance like the sensor manager
                    // our address is in the data bytes. Check which byte and then check, if the correspondent bit is set.
                    //log.trace("multiple occupation was signalled, states: {}, BM base Number: {}, BM count: {}, node: {}", ByteUtils.bytesToHex(detectorData), baseAddress, detectorCount, addr);
                    int relAddr = addr.getAddr() - baseAddress;
                    byte b = detectorData[ relAddr / 8];
                    boolean isOccupied = (b & (1 << (relAddr % 8))) != 0;
                    log.debug("REPORTER multi occupation was signalled, state: {}, BM addr: {}, node: {}", isOccupied ? "OCCUPIED" : "FREE", addr.getAddr(), addr);
                    if (!isOccupied) {
                        synchronized(entrySet) {
                            entrySet.clear();
                        }
                        notify_loco(null);
                    }
                }
            }
        };
        tc.addMessageListener(messageListener);        
    }
    
    // Collecting Reporter Interface methods
    /**
      * {@inheritDoc}
      */
     @Override
     public java.util.Collection<Object> getCollection(){
        return entrySet;
     }

    /**
     * {@inheritDoc}
     * 
     * Remove the Message Listener for this reporter
     */
    @Override
    public void dispose() {
        if (messageListener != null) {
            tc.removeMessageListener(messageListener);        
            messageListener = null;
        }
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(BiDiBReporter.class);

}

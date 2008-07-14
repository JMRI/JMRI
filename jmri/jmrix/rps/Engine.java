// Engine.java
 
package jmri.jmrix.rps;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

// for F2 hack
import jmri.DccThrottle;
import jmri.InstanceManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Engine does basic computations of RPS system.
 *<p>
 * Holds all the alignment info. Receiver numbers are indexed from one.
 *<p>
 * Gets a reading from the Distributor and passes back a Measurement
 *<p>
 * Bound properties:
 *<ul>
 *<li>vSound - velocity of sound, in whatever units are in use
 *</ul>
 *
 * @author	   Bob Jacobsen   Copyright (C) 2006, 2008
 * @version   $Revision: 1.14 $
 */


public class Engine implements ReadingListener {

    public Engine() {
    }
    
    void loadValues() {
        // load dummy contents
        setInitialAlignment();
        loadInitialTransmitters();
    }
    
    public void dispose() {
    }

    public void setVSound(double v) {
        double oldVal = vsound;
        vsound = v;
        log.info("change vsound from "+oldVal+" to "+v);
        prop.firePropertyChange("vSound", new Double(oldVal), new Double(v));
    }
    public double getVSound() {
        return vsound;
    }
    private double vsound = 0.013544;  // 0.013544 inches/usec, .000345 m/usec, 
    private int offset = 0;
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public int getOffset() {
        return offset;
    }
        
    Measurement lastPoint = null;
    
    Receiver[] receivers;
    
    /**
     * Set the number of receivers
     */
    public void setReceiverCount(int n) {
        log.debug("setReceiverCount to "+n);
        if ((receivers!=null) && (n == receivers.length+1)) return;
        Receiver[] oldReceivers = receivers;
        receivers = new Receiver[n+1];  // n is highest address, so need n+1
        if (oldReceivers == null) return;
        // copy the existing receivers
        for (int i=0; i<Math.min(n+1, oldReceivers.length); i++)
            receivers[i] = oldReceivers[i];
    }
    
    public int getReceiverCount() {
        return receivers.length-1;
    }
    
    /**
     * Get a particular reciever by address (starting at 1)
     */
    public void setReceiver(int address, Receiver receiver) {
        if (receivers == null) throw new IllegalArgumentException("Must initialize first");
        if (address>=receivers.length) throw new IllegalArgumentException("Index "+address+" is larger than expected "+receivers.length);
        log.debug("store receiver "+address+" in "+this);
        receivers[address] = receiver;
    }
    
    public Receiver getReceiver(int i) {
        return receivers[i];
    }

    public void setReceiverPosition(int i, Point3d p) {
        receivers[i].setPosition(p);
    }
    
    public Point3d getReceiverPosition(int i) {
        if (receivers[i] == null) { 
            log.debug("getReceiverPosition of null receiver index i="+i);
            return null;
        }
        return receivers[i].getPosition();
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    
    String algorithm = "Ash 2.1";
    
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work done.
        
        int count = 0;
        for (int i = 0; i<receivers.length; i++) {
            if (receivers[i]!=null) count++;
        }
                
        int index = 0;
        Point3d list[] = new Point3d[count];
        for (int i = 0; i<receivers.length; i++) {
            Point3d p = getReceiverPosition(i);
            if ( p != null ) {
                list[index] = p;
                index++;
            }
        }
        
        Calculator c = Algorithms.newCalculator(list, getVSound(), 
                            getOffset(), algorithm);

        Measurement m = c.convert(r, lastPoint);

        saveLastMeasurement(r.getID(), m);
        
        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }
    
    // Store the lastMeasurement 
    void saveLastMeasurement(int addr, Measurement m) {
        for (int i=0; i<getNumTransmitters(); i++) {
            if (getTransmitter(i).getAddress() == addr) {
                getTransmitter(i).setLastMeasurement(m);
                // might be more than one, so don't end here
            }
        }
    }
    
    // Store alignment info
    public void storeAlignment(File file) throws org.jdom.JDOMException, IOException {
        PositionFile pf = new PositionFile();
        pf.prepare();
        pf.setConstants(getVSound(), getOffset());
        
        for (int i = 1; i<=getReceiverCount(); i++) {
            if (getReceiver(i) == null) continue;
            pf.setReceiver(i, getReceiver(i));
        }
        pf.store(file);
    }
    
    public void loadAlignment(File file) throws org.jdom.JDOMException, IOException {
        // start by getting the file
        PositionFile pf = new PositionFile();
        pf.loadFile(file);
        
        // get VSound
        setVSound(pf.getVSound());
        
        // get offset
        setOffset(pf.getOffset());
        
        // get receivers
        setReceiverCount(pf.maxReceiver());  // count from 1
        Point3d p;
        for (int i = 1; i<=getReceiverCount(); i++) {    
            p = pf.getReceiverPosition(i);
            if (p == null) continue;
            log.debug("load "+i+" with "+p);
            setReceiver(i, new Receiver(p));
        }
        
    }
    
    void setInitialAlignment() {
        File defaultFile = new File(PositionFile.defaultFilename());
        try {
            loadAlignment(defaultFile);
        } catch (Exception e) {
            log.debug("load exception"+e);
            // load dummy values
            setReceiverCount(2);
            setReceiver(0, new Receiver(new Point3d(0.0,0.0,72.0)));
            setReceiver(1, new Receiver(new Point3d(72.0,0.0,72.0)));
        }                
    }
    
    //**************************************
    // Methods to handle polling
    //**************************************
    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
    int pollingInterval = 500;
    public int getPollingInterval() { return pollingInterval; }
    
    boolean polling = false;
    public void setPolling(boolean polling) {
        this.polling = polling;
        if (polling) startpoll();
        else stoppoll();
    }
    public boolean getPolling() { return polling; }
    
    java.util.ArrayList transmitters;
    
    void loadInitialTransmitters() {
        transmitters = new java.util.ArrayList();
        // put in two dummies
        transmitters.add(new Transmitter("sample 1", true, 1234, true));
        transmitters.add(new Transmitter("sample 2", false, 4321, true));
        // add the roster
        java.util.List l = Roster.instance().matchingList(null, null, null, null, null, null, null);
        log.debug("Got "+l.size()+" roster entries");
        for (int i=0; i<l.size(); i++) {
            RosterEntry r = null;
            try {
                r = (RosterEntry)l.get(i);
                int address = Integer.parseInt(r.getDccAddress());
                Transmitter t = new Transmitter(r.getId(), false, address, r.isLongAddress());
                transmitters.add(t);
            } catch (Exception e) {
                // just skip this entry
                if (r!=null)
                    log.warn("Skip roster entry: "+r.getId());
                else
                    log.warn("Failed roster entry skipped");
            } 
        }
        
        // get polling status, if possible
        try {
            loadPollConfig(new File(PollingFile.defaultFilename()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Store polling info
    public void storePollConfig(File file) throws org.jdom.JDOMException, IOException {
        PollingFile pf = new PollingFile();
        pf.prepare();
        pf.setPoll();
        
        for (int i = 0; i<getNumTransmitters(); i++) {
            pf.setTransmitter(i);
        }
        pf.store(file);
    }
    
    public void loadPollConfig(File file) throws org.jdom.JDOMException, IOException {
        PollingFile pf = new PollingFile();
        pf.loadFile(file);  
        // first make sure transmitters defined      
        pf.getTransmitters(this);
        // and possibly start polling
        pf.getPollValues();
    }    

    public Transmitter getTransmitter(int i) { 
        if (i<0) return null;
        if (transmitters == null) return null;
        return (Transmitter) transmitters.get(i);
    }
    public int getNumTransmitters() { 
        if (transmitters == null) return 0;
        return transmitters.size();
    }
    
    public int getPolledAddress() {
        Transmitter t = getTransmitter(pollIndex);
        if (t==null) return -1;
        return t.getAddress();
    }
    
    /**
     * The real core of the polling, this selects the next one to 
     * poll. -1 means none selected, try again later.
     */
    int selectNextPoll() {
        int startindex = pollIndex;
        while (++pollIndex < getNumTransmitters()) {
            if (getTransmitter(pollIndex).isPolled()) return pollIndex;
        } 
        // here, we got to the end without finding somebody to poll
        // try the start
        pollIndex = -1; // will autoincrement to 0
        while (++pollIndex <= startindex) {
            if (getTransmitter(pollIndex).isPolled()) return pollIndex;
        } 
        // no luck, say so
        return -1;
    }
    
    int pollIndex = -1; // left at last one done
    boolean bscPoll = false;
    boolean throttlePoll = false;
    
    public void setBscPollMode() { 
        bscPoll = true;
        throttlePoll = false;
    }
    public void setDirectPollMode() { 
        bscPoll = false;
        throttlePoll = false;
    }
    public void setThrottlePollMode() { 
        bscPoll = false;
        throttlePoll = true;
    }
    public boolean getBscPollMode() { return bscPoll; }
    public boolean getThrottlePollMode() { return throttlePoll; }
    public boolean getDirectPollMode() { return !(bscPoll || throttlePoll); }
        
    void startpoll() {
        // time to start operation
        pollThread = new Thread(){
            public void run() {
                log.debug("Polling starts");
                while (true) {
                    try {
                        int i = selectNextPoll();
                        log.debug("Poll "+i);
                        setOn(i);
                        synchronized (this) { wait(20); }
                        setOff(i);
                        synchronized (this) { wait(pollingInterval); }
                    } catch (InterruptedException e) { 
                        // cancel whatever is happening
                        log.debug("Polling stops");
                        return; // end operation
                    }
                }
            }
        };
        pollThread.start();
    }
    
    Thread pollThread;
    
    void stoppoll() {
        if (pollThread != null) pollThread.interrupt();
    }

    void setOn(int i) {
        Transmitter t = getTransmitter(i);
        byte[] packet;
        if (bscPoll) {
            // poll using BSC instruction
            packet = jmri.NmraPacket.threeBytePacket(
                                t.getAddress(), t.isLongAddress(), 
                                (byte)0xC0, (byte)0xA5, (byte)0xFE);
            if (jmri.InstanceManager.commandStationInstance() != null)
                jmri.InstanceManager.commandStationInstance().sendPacket(packet, 1);
        } else {
            // poll using F2
            if (throttlePoll) {
                // use throttle; first, get throttle
                if (t.checkInit()) {
                    // now send F2
                    t.getThrottle().setF2(true);
                } else return;  // bail if not ready
            } else {
                // send packet direct
                packet = jmri.NmraPacket.function0Through4Packet(
                                t.getAddress(), t.isLongAddress(),
                                false, false, true, false, false);
                if (jmri.InstanceManager.commandStationInstance() != null)
                    jmri.InstanceManager.commandStationInstance().sendPacket(packet, 1);
            }
        }
    }
    void setOff(int i) {
    if (!bscPoll) {
            // have to turn off F2 since not using BSC
            Transmitter t = getTransmitter(i);
            if (throttlePoll) {
                // use throttle; first, get throttle
                if (t.checkInit()) {
                    // now send F2
                    t.getThrottle().setF2(false);
                } else return;  // bail if not ready
            } else {
                // send direct
                byte[] packet = jmri.NmraPacket.function0Through4Packet(
                                    t.getAddress(), t.isLongAddress(),
                                    false, false, false, false, false);
                if (jmri.InstanceManager.commandStationInstance() != null)
                    jmri.InstanceManager.commandStationInstance().sendPacket(packet, 1);
            }
        }
    }
    
    public class Transmitter implements jmri.ThrottleListener {
        Transmitter(String id, boolean polled, int address, boolean longAddress) {
            setID(id);
            setPolled(polled);
            setAddress(address);
            setLongAddress(longAddress);
        }
        
        public String getID() { return id; }
        String id;
        public void setID(String id) { this.id = id; }
        
        public boolean isLongAddress() { return longAddress; }
        boolean longAddress;
        public void setLongAddress(boolean longAddress) { this.longAddress = longAddress; }
        
        public int getAddress() { return address; }
        int address;
        public void setAddress(int address) { this.address = address; }
        
        public boolean isPolled() { return polled; }
        boolean polled;
        public void setPolled(boolean polled) { this.polled = polled; }
        
        Measurement lastMeasurement = null;
        public void setLastMeasurement(Measurement last) { lastMeasurement = last; }
        public Measurement getLastMeasurement() { return lastMeasurement; }
    
        // stuff to do F2 poll
        DccThrottle throttle;
        boolean needReqThrottle = true;
        
        DccThrottle getThrottle() { return throttle; }
        boolean checkInit() {
            if (throttle != null) return true;
            if (!needReqThrottle) return false;
            // request throttle
            InstanceManager.throttleManagerInstance().requestThrottle(address,longAddress, this);
            return false;
        }

        public void notifyThrottleFound(DccThrottle t) {
            needReqThrottle = false;
            throttle = t;
        }
    }

    // for now, we only allow one Engine
    static protected Engine _instance = null;
    static public Engine instance() {
        if (_instance == null) {
            _instance = new Engine();
            _instance.loadValues();
        }
        return _instance;
    }

    // handle outgoing parameter notification
    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Engine.class.getName());
}

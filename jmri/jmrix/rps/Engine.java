// Engine.java
 
package jmri.jmrix.rps;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

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
 *
 * @author	   Bob Jacobsen   Copyright (C) 2006, 2008
 * @version   $Revision: 1.1 $
 */


public class Engine implements ReadingListener {

    public Engine() {
    
        // load dummy contents
        setInitialAlignment();
        loadInitialTransmitters();
    }
    
    public void dispose() {
    }

    public void setVSound(double v) {
        vsound = v;
    }
    public double getVSound() {
        return vsound;
    }
    private double vsound = 0.013544;  // 0.013544 inches/usec, .000345 m/usec, 
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public int getOffset() {
        return offset;
    }
    private int offset;
        
    Measurement lastPoint = null;
    
    Receiver[] receivers;
    
    /**
     * Set the number of receivers (highest address, since they start from one)
     */
    public void setReceiverCount(int n) {
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
        receivers[address] = receiver;
    }
    
    public Receiver getReceiver(int i) {
        return receivers[i];
    }

    public void setReceiverPosition(int i, Point3d p) {
        receivers[i].setPosition(p);
    }
    
    public Point3d getReceiverPosition(int i) {
        if (receivers[i] == null) return null;
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

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }
    
    // load and store
    public void store(File file) throws org.jdom.JDOMException, IOException {
        PositionFile pf = new PositionFile();
        pf.prepare();
        pf.setConstants(getVSound(), getOffset());
        
        for (int i = 1; i<=getReceiverCount(); i++) {
            if (getReceiver(i) == null) continue;
            pf.setReceiver(i, getReceiver(i));
        }
        pf.store(file);
    }
    
    public void load(File file) throws org.jdom.JDOMException, IOException {
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
            load(defaultFile);
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
    
    public void setPolling(boolean polling) {
        if (polling) startpoll();
        else stoppoll();
    }
        
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
            RosterEntry r = (RosterEntry)l.get(i);
            int address = Integer.parseInt(r.getDccAddress());
            Transmitter t = new Transmitter(r.getId(), false, address, r.isLongAddress());
            transmitters.add(t);
        }
    }
    
    public Transmitter getTransmitter(int i) { 
        if (i<0) return null;
        if (transmitters == null) return null;
        return (Transmitter) transmitters.get(i);
    }
    public int getNumTransmitters() { return transmitters.size(); }
    
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
    
    void startpoll() {
        log.debug("start poll");
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
        byte[] packet = jmri.NmraPacket.function0Through4Packet(
                                t.getAddress(), t.isLongAddress(),
                                false, false, true, false, false);
        jmri.InstanceManager.commandStationInstance().sendPacket(packet, 3);
    }
    void setOff(int i) {
        Transmitter t = getTransmitter(i);
        byte[] packet = jmri.NmraPacket.function0Through4Packet(
                                t.getAddress(), t.isLongAddress(),
                                false, false, false, false, false);
        jmri.InstanceManager.commandStationInstance().sendPacket(packet, 3);
    }
    
    public class Transmitter {
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
        
    }

    // for now, we only allow one Engine
    static Engine _instance = null;
    static public Engine instance() {
        if (_instance == null) _instance = new Engine();
        return _instance;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Engine.class.getName());
}

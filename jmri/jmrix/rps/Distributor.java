// Distributor.java

package jmri.jmrix.rps;

import java.util.Vector;

/**
 * Distributes Readings and the Measurements calculated from them.
 * <P>
 * @author	Bob Jacobsen  Copyright (C) 2006, 2008
 *
 * @version	$Revision: 1.3 $
 */
public class Distributor {

    /**
     * Request being informed when a new Reading 
     * is available.
     */
    public void addReadingListener(ReadingListener l) {
        // add only if not already registered
        if (!readingListeners.contains(l)) {
            readingListeners.addElement(l);
        }
    }

    /**
     * Request to no longer be informed when new Readings arrive.
     */
    public void removeReadingListener(ReadingListener l) {
        if (readingListeners.contains(l)) {
            readingListeners.removeElement(l);
        }
    }
    
    /**
     * Invoked when a new Reading is created
     */
    public void submitReading(Reading s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this) {
            v = (Vector) readingListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" ReadingListeners about item ");
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            ReadingListener client = (ReadingListener) v.elementAt(i);
            javax.swing.SwingUtilities.invokeLater(new ForwardReading(s, client));
        }
    }
    
    /**
     * Request being informed when a new Measurement 
     * is available.
     */
    public void addMeasurementListener(MeasurementListener l) {
        // add only if not already registered
        if (!measurementListeners.contains(l)) {
            measurementListeners.addElement(l);
        }
    }

    /**
     * Request to no longer be informed when new Measurements arrive.
     */
    public void removeMeasurementListener(MeasurementListener l) {
        if (measurementListeners.contains(l)) {
            measurementListeners.removeElement(l);
        }
    }

    /**
     * Invoked when a new Measurement is created
     */
    public void submitMeasurement(Measurement s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this) {
                v = (Vector) measurementListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" MeasurementListeners about item ");
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            MeasurementListener client = (MeasurementListener) v.elementAt(i);
            javax.swing.SwingUtilities.invokeLater(new ForwardMeasurement(s, client));
        }
    }
    
    static private Distributor instance = null;
    
    public static Distributor instance() {
        if (instance == null) instance = new Distributor();
        return instance;
    }

    ////////////////////////////
    // Implementation details //
    ////////////////////////////

    final private Vector readingListeners = new Vector();
    final private Vector measurementListeners = new Vector();

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Distributor.class.getName());

    /**
     * Forward the Reading from the Swing thread
     */
    class ForwardReading implements Runnable {
    
        Reading s;
        ReadingListener client;
        
        ForwardReading(Reading s, ReadingListener client) {
            this.s = s;
            this.client = client;
        }
        
        public void run() {
            client.notify(s);
        }
    }

    /**
     * Forward the Measurement from the Swing thread
     */
    class ForwardMeasurement implements Runnable {
    
        Measurement s;
        MeasurementListener client;
        
        ForwardMeasurement(Measurement s, MeasurementListener client) {
            this.s = s;
            this.client = client;
        }
        
        public void run() {
            client.notify(s);
        }
    }

}

/* @(#)Distributor.java */

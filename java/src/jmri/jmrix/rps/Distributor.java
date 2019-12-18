package jmri.jmrix.rps;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributes Readings and the Measurements calculated from them.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class Distributor {

    /**
     * Request being informed when a new Reading is available.
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
     * Invoked when a new Reading is created.
     */
    @SuppressWarnings("unchecked")
    public void submitReading(Reading s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<ReadingListener> v;
        synchronized (this) {
            v = (Vector<ReadingListener>) readingListeners.clone();
        }
        log.debug("notify {} ReadingListeners about item", v.size());
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            ReadingListener client = v.elementAt(i);
            javax.swing.SwingUtilities.invokeLater(new ForwardReading(s, client));
        }
    }

    /**
     * Request being informed when a new Measurement is available.
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
     * Invoked when a new Measurement is created.
     */
    @SuppressWarnings("unchecked")
    public void submitMeasurement(Measurement s) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<MeasurementListener> v;
        synchronized (this) {
            v = (Vector<MeasurementListener>) measurementListeners.clone();
        }
        log.debug("notify {} MeasurementListeners about item", v.size());
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            MeasurementListener client = v.elementAt(i);
            javax.swing.SwingUtilities.invokeLater(new ForwardMeasurement(s, client));
        }
    }

    static volatile private Distributor instance = null;

    public static Distributor instance() {
        if (instance == null) {
            instance = new Distributor();
        }
        return instance;
    }

    ////////////////////////////
    // Implementation details //
    ////////////////////////////
    final private Vector<ReadingListener> readingListeners = new Vector<ReadingListener>();
    final private Vector<MeasurementListener> measurementListeners = new Vector<MeasurementListener>();

    /**
     * Forward the Reading from the Swing thread.
     */
    static class ForwardReading implements Runnable {

        Reading s;
        ReadingListener client;

        ForwardReading(Reading s, ReadingListener client) {
            this.s = s;
            this.client = client;
        }

        @Override
        public void run() {
            client.notify(s);
        }
    }

    /**
     * Forward the Measurement from the Swing thread.
     */
    static class ForwardMeasurement implements Runnable {

        Measurement s;
        MeasurementListener client;

        ForwardMeasurement(Measurement s, MeasurementListener client) {
            this.s = s;
            this.client = client;
        }

        @Override
        public void run() {
            client.notify(s);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Distributor.class);

}

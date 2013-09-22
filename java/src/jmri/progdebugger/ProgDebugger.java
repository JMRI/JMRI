// ProgDebugger.java

package jmri.progdebugger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;

/**
 * Debugging implementation of Programmer interface.
 *<P>
 * Remembers writes, and returns the last written value
 * when a read to the same CV is made.
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2007
 * @version         $Revision$
 */
public class ProgDebugger implements Programmer  {

    // write CV is recorded for later use
    private int _lastWriteVal = -1;
    private int _lastWriteCv = -1;
    public int lastWrite() { return _lastWriteVal; }
    public int lastWriteCv() { return _lastWriteCv; }

    /**
     * Reset the CV to a value so one
     * can detect if it's been written.
     * <p>
     * Does not change the "lastWrite" and "lastWriteCv" results.
     */
    public void resetCv(int cv, int val) {
        mValues.put(Integer.valueOf(cv), Integer.valueOf(val));
    }
    
    /**
     * Get the CV value directly, without going through
     * the usual indirect protocol. Used for e.g. testing.
     * <p>
     * Does not change the "lastRead" and "lastReadCv" results.
     */
    public int getCvVal(int cv) {
        // try to get something from hash table
        Integer saw = (mValues.get(Integer.valueOf(cv)));
        if (saw!=null) return saw.intValue();
        log.warn("CV "+cv+" has no defined value");
        return -1;
    }
    
    // write CV values are remembered for later reads
    Hashtable<Integer,Integer> mValues = new Hashtable<Integer,Integer>();

    public String decodeErrorCode(int i) {
        log.debug("decoderErrorCode "+i);
        return "error "+i;
    }

    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException
    {
        final ProgListener m = p;
        // log out the request
        log.info("write CV: "+CV+" to: "+val+" mode: "+getMode());
        _lastWriteVal = val;
        _lastWriteCv = CV;
        // save for later retrieval
        mValues.put(Integer.valueOf(CV), Integer.valueOf(val));

        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
                ProgListener l = m;
                public void run() {
                    log.debug("write CV reply");
                    if (l!=null) l.programmingOpReply(-1, 0); }  // 0 is OK status
            };
        sendReturn(r);
    }

    // read CV values
    // note that the hashTable will be used if the CV has been written
    private int _nextRead = 123;
    public void nextRead(int r) { _nextRead = r; }

    private int _lastReadCv = -1;
    public int lastReadCv() { return _lastReadCv; }

    boolean confirmOK;  // cached result of last compare

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        final ProgListener m = p;
        
        // guess by comparing current value in val to has table
        Integer saw = mValues.get(Integer.valueOf(CV));
        int result = -1; // what was read
        if (saw!=null) { 
            result = saw.intValue();
            confirmOK = (result==val);
            log.info("confirm CV: "+CV+" mode: "+getMode()+" will return "+result+" pass: "+confirmOK);
        } else {
            result = -1;
            confirmOK = false;
            log.info("confirm CV: "+CV+" mode: "+getMode()+" will return -1 pass: false due to no previous value");
        }
        _lastReadCv = CV;
        // return a notification via the queue to ensure end
        final int returnResult = result;  // final to allow passing to inner class
        Runnable r = new Runnable() {
                ProgListener l = m;
                int result = returnResult; 
                public void run() {
                    log.debug("read CV reply");
                    if (confirmOK) l.programmingOpReply(result, ProgListener.OK);
                    else l.programmingOpReply(result, ProgListener.ConfirmFailed);
                }
            };
        sendReturn(r);

    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        final ProgListener m = p;
        _lastReadCv = CV;

        // try to get something from hash table
        Integer saw = mValues.get(Integer.valueOf(CV));
        if (saw!=null) _nextRead = saw.intValue();

        log.info("read CV: "+CV+" mode: "+getMode()+" will read "+_nextRead);

        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
                ProgListener l = m;
                public void run() {
                    // log.debug("read CV reply - start sleep");
                    // try { Thread.sleep(100); } catch (Exception e) {}
                    log.debug("read CV reply");
                    l.programmingOpReply(_nextRead, 0); }  // 0 is OK status
            };
        sendReturn(r);

    }

    // handle mode - default is paged mode
    protected int _mode = Programmer.PAGEMODE;

    public void setMode(int mode) {
        log.debug("setMode: old="+_mode+" new="+mode);
        if (mode != _mode) {
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
    }
    public int getMode() { return _mode; }
    public boolean hasMode(int mode) {
        log.debug("pretending to have mode "+mode);
        return true;
    }

    public boolean getCanRead() { return true; }

    /**
     * By default, the highest test CV is 256 so that
     * we can test composite operations
     */
    public int getMaxCvAddr() { return 256; }

    // data members to hold contact with the property listeners
    private Vector<PropertyChangeListener> propListeners = new Vector<PropertyChangeListener>();

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!propListeners.contains(l)) {
            propListeners.addElement(l);
        }
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (propListeners.contains(l)) {
            propListeners.removeElement(l);
        }
    }

    @SuppressWarnings("unchecked")
	protected void notifyPropertyChange(String name, int oldval, int newval) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>)propListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    static final boolean IMMEDIATERETURN = true;
    
    /**
     * Arrange for the return to be invoked on the Swing thread.
     */
    void sendReturn(Runnable run) {
        if (IMMEDIATERETURN) {
            javax.swing.SwingUtilities.invokeLater(run);
        } else {
            javax.swing.Timer timer = new javax.swing.Timer(2, null);
            java.awt.event.ActionListener l = new java.awt.event.ActionListener(){
                javax.swing.Timer timer;
                Runnable run;
                java.awt.event.ActionListener init(javax.swing.Timer t, Runnable r) {
                    this.timer = t;
                    this.run = r;
                    return this;
                }
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    this.timer.stop();
                    javax.swing.SwingUtilities.invokeLater(run);
                }
            }.init(timer, run);
            timer.addActionListener(l);
            timer.start();
        }
    }
    
    static Logger log = LoggerFactory.getLogger(ProgDebugger.class.getName());
}

/* @(#)ProgDebugger.java */

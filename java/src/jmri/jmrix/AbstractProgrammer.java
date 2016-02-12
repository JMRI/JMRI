// AbstractProgrammer.java
package jmri.jmrix;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.annotation.Nonnull;
import java.util.List;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementations for the Programmer interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2012, 2013
 * @version $Revision$
 */
public abstract class AbstractProgrammer implements Programmer {

    public String decodeErrorCode(int code) {
        if (code == ProgListener.OK) {
            return Bundle.getMessage("StatusOK");
        }
        StringBuffer sbuf = new StringBuffer("");
        // add each code; terminate each string with ";" please.
        if ((code & ProgListener.NoLocoDetected) != 0) {
            sbuf.append(Bundle.getMessage("NoLocoDetected") + " ");
        }
        if ((code & ProgListener.ProgrammerBusy) != 0) {
            sbuf.append(Bundle.getMessage("ProgrammerBusy") + " ");
        }
        if ((code & ProgListener.NotImplemented) != 0) {
            sbuf.append(Bundle.getMessage("NotImplemented") + " ");
        }
        if ((code & ProgListener.UserAborted) != 0) {
            sbuf.append(Bundle.getMessage("UserAborted") + " ");
        }
        if ((code & ProgListener.ConfirmFailed) != 0) {
            sbuf.append(Bundle.getMessage("ConfirmFailed") + " ");
        }
        if ((code & ProgListener.FailedTimeout) != 0) {
            sbuf.append(Bundle.getMessage("FailedTimeout") + " ");
        }
        if ((code & ProgListener.UnknownError) != 0) {
            sbuf.append(Bundle.getMessage("UnknownError") + " ");
        }
        if ((code & ProgListener.NoAck) != 0) {
            sbuf.append(Bundle.getMessage("NoAck") + " ");
        }
        if ((code & ProgListener.ProgrammingShort) != 0) {
            sbuf.append(Bundle.getMessage("ProgrammingShort") + " ");
        }
        if ((code & ProgListener.SequenceError) != 0) {
            sbuf.append(Bundle.getMessage("SequenceError") + " ");
        }
        if ((code & ProgListener.CommError) != 0) {
            sbuf.append(Bundle.getMessage("CommError") + " ");
        }

        // remove trailing separators
        if (sbuf.length() > 2) {
            sbuf.setLength(sbuf.length() - 2);
        }

        String retval = sbuf.toString();
        if (retval.equals("")) {
            return "unknown status code: " + code;
        } else {
            return retval;
        }
    }

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void notifyPropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        writeCV(Integer.parseInt(CV), val, p);
    }

    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        readCV(Integer.parseInt(CV), p);
    }

    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(Integer.parseInt(CV), val, p);
    }

    /**
     * Basic implementation. Override this to turn reading on and off globally.
     */
    public boolean getCanRead() {
        return true;
    }

    /**
     * Checks using the current default programming mode
     */
    public boolean getCanRead(String addr) {
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        return Integer.parseInt(addr) <= 1024;
    }

    // handle mode
    private ProgrammingMode mode = null;

    @Override
    public final void setMode(ProgrammingMode m) {
        List<ProgrammingMode> validModes = getSupportedModes();
        
        if (m == null) {
            if (validModes.size()>0) {
                // null can only be set if there are no valid modes
                throw new IllegalArgumentException("Cannot set null mode when modes are present");
            } else {
                mode = null;
            }
        }
        
        if (validModes.contains(m)) {
            ProgrammingMode oldMode = mode;
            mode = m;
            notifyPropertyChange("Mode", oldMode, m);
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m);
        }
    }

    /**
     * Define the "best" programming mode, which provides the initial setting.
     *
     * The definition of "best" is up to the specific-system developer.
     * By default, this is the first of the available methods from getSupportedModes;
     * override this method to change that.
     */ 
    public ProgrammingMode getBestMode() {
        return getSupportedModes().get(0);
    }

    public final ProgrammingMode getMode() {
        if (mode == null) {
            mode = getBestMode();
        }
        return mode;
    }

    @Override
    abstract public  @Nonnull List<ProgrammingMode> getSupportedModes();

    /**
     * Basic implementation. Override this to turn writing on and off globally.
     */
    public boolean getCanWrite() {
        return true;
    }

    /**
     * Checks using the current default programming mode.
     */
    public boolean getCanWrite(String addr) {
        return getCanWrite();
    }

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startShortTimer() {
        restartTimer(SHORT_TIMEOUT);
    }

    /**
     * Internal routine to restart timer with a long delay
     */
    protected void startLongTimer() {
        restartTimer(LONG_TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well
     */
    protected synchronized void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts & restarts
     */
    protected synchronized void restartTimer(int delay) {
        if (log.isDebugEnabled()) {
            log.debug("restart timer with delay " + delay);
        }
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    timeout();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Find the register number that corresponds to a specific CV number.
     *
     * @throws ProgrammerException if the requested CV does not correspond to a
     *                             register
     * @param cv CV number (1 through 512) for which equivalent register is
     *           desired
     * @return register number corresponding to cv
     */
    public int registerFromCV(int cv) throws ProgrammerException {
        if (cv <= 4) {
            return cv;
        }
        switch (cv) {
            case 29:
                return 5;
            case 7:
                return 7;
            case 8:
                return 8;
        }
        throw new ProgrammerException();
    }

    /**
     * Internal routine to handle a timeout, should be synchronized!
     */
    abstract protected void timeout();

    protected int SHORT_TIMEOUT = 2000;
    protected int LONG_TIMEOUT = 60000;

    javax.swing.Timer timer = null;

    private final static Logger log = LoggerFactory.getLogger(AbstractProgrammer.class.getName());

}

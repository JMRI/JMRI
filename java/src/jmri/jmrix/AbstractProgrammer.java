package jmri.jmrix;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Common implementations for the Programmer interface.
 * <p>
 * Contains two time-out handlers:
 * <ul>
 * <li> SHORT_TIMEOUT, the "short timer", is on operations other than reads
 * <li> LONG_TIMEOUT, the "long timer", is for the "read from decoder" step, which can take a long time.
 * </ul>
 * The duration of these can be adjusted by changing the values of those constants in subclasses.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2012, 2013
 */
public abstract class AbstractProgrammer implements Programmer {

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void notifyPropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    /** {@inheritDoc} */
    @Override
    abstract public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException;

    /** {@inheritDoc} */
    @Override
    abstract public void readCV(String CV, ProgListener p) throws ProgrammerException;

    /** {@inheritDoc} */
    @Override
    abstract public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException;


    /** {@inheritDoc} 
     * Basic implementation. Override this to turn reading on and off globally.
     */
    @Override
    public boolean getCanRead() {
        return true;
    }

    /** {@inheritDoc} 
     * Checks using the current default programming mode
     */
    @Override
    public boolean getCanRead(String addr) {
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        return Integer.parseInt(addr) <= 1024;
    }

    // handle mode
    private ProgrammingMode mode = null;

    /** {@inheritDoc} */
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
     * <p>
     * The definition of "best" is up to the specific-system developer.
     * By default, this is the first of the available methods from getSupportedModes;
     * override this method to change that.
     * 
     * @return The recommended ProgrammingMode or null if none exists or is defined.
     */ 
    public ProgrammingMode getBestMode() {
        if (!getSupportedModes().isEmpty()) {
            return getSupportedModes().get(0);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final ProgrammingMode getMode() {
        if (mode == null) {
            mode = getBestMode();
        }
        return mode;
    }

    @Override
    abstract @Nonnull public List<ProgrammingMode> getSupportedModes();

    /** {@inheritDoc} 
     * Basic implementation. Override this to turn writing on and off globally.
     */
    @Override
    public boolean getCanWrite() {
        return true;
    }

    /** {@inheritDoc} 
     * Checks using the current default programming mode.
     */
    @Override
    public boolean getCanWrite(String addr) {
        return getCanWrite();
    }

    /** {@inheritDoc} 
     * By default, say that no verification is done.
     *
     * @param addr A CV address to check (in case this varies with CV range) or null for any
     * @return Always WriteConfirmMode.NotVerified
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.NotVerified; }
    

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startShortTimer() {
        log.debug("startShortTimer");
        restartTimer(SHORT_TIMEOUT);
    }

    /**
     * Internal routine to restart timer with a long delay
     */
    protected void startLongTimer() {
        log.debug("startLongTimer");
        restartTimer(LONG_TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well
     */
    protected synchronized void stopTimer() {
        log.debug("stop timer");
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts
     */
    protected synchronized void restartTimer(int delay) {
        log.debug("(re)start timer with delay {}", delay);

        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                @Override
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
     * @param cv CV number (1 through 512) for which equivalent register is
     *           desired
     * @throws ProgrammerException if the requested CV does not correspond to a
     *                             register
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
            default:
                log.warn("Unhandled register from cv: {}", cv);
                break;
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractProgrammer.class);

}

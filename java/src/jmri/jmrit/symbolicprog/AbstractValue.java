package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Define common base class methods for CvValue and VariableValue classes
 * <p>
 * The ToRead parameter (boolean, unbound) is used to remember whether this
 * object has been read during a "read all" operation. This allows removal of
 * duplicate operations.
 * <p>
 * The ToWrite parameter (boolean, unbound) is used to remember whether this
 * object has been written during a "write all" operation. This allows removal
 * of duplicate operations.
 * <p>
 * The Available parameter (boolean, unbound) remembers whether the variable
 * should be displayed, programmed, etc.
 * <p>
 * Description: Represents a single CV value
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005
 */
public abstract class AbstractValue {

    PropertyChangeSupport prop = new PropertyChangeSupport(this);

    /**
     * Method to handle color changes for states.
     *
     * @param c the desired colour
     */
    abstract void setColor(Color c);

    /**
     * Defines state when nothing is known about the real value.
     */
    public static final int UNKNOWN = 0;

    /**
     * Defines state where value has been edited, no longer same as in decoder
     * or file.
     */
    public static final int EDITED = 4;

    /**
     * Defines state where value has been read from (hence same as) decoder, but
     * perhaps not same as in file.
     */
    public static final int READ = 16;

    /**
     * Defines state where value has been written to (hence same as) decoder,
     * but perhaps not same as in file.
     */
    public static final int STORED = 64;

    /**
     * Defines state where value was read from a config file, but might not be
     * the same as the decoder.
     */
    public static final int FROMFILE = 256;

    /**
     * Defines state where value was read from a config file, and is the same as
     * the decoder.
     */
    public static final int SAME = 512;

    /**
     * Defines state where value was read from a config file, and is the not the
     * same as the decoder.
     */
    public static final int DIFF = 1024;

    /**
     * Define color to denote UNKNOWN state. null means to use default for the
     * component.
     */
    static final Color COLOR_UNKNOWN = Color.red.brighter();

    /**
     * Define color to denote EDITED state. null means to use default for the
     * component.
     */
    static final Color COLOR_EDITED = Color.orange;

    /**
     * Define color to denote READ state. null means to use default for the
     * component.
     */
    static final Color COLOR_READ = null;

    /**
     * Define color to denote STORED state. null means to use default for the
     * component.
     */
    static final Color COLOR_STORED = null;

    /**
     * Define color to denote FROMFILE state. null means to use default for the
     * component.
     */
    static final Color COLOR_FROMFILE = Color.yellow;

    /**
     * Define color to denote SAME state. null means to use default for the
     * component.
     */
    static final Color COLOR_SAME = null;

    /**
     * Define color to denote DIFF state. null means to use default for the
     * component.
     */
    static final Color COLOR_DIFF = Color.red.brighter();

    /**
     * Mark whether this object needs to be read.
     *
     * @param state true if the object needs to be read, false otherwise
     * @see AbstractValue
     */
    public void setToRead(boolean state) {
        _toRead = state;
    }

    /**
     * Ask whether this object needs to be read.
     *
     * @return true if the object needs to be read, false otherwise
     * @see AbstractValue
     */
    public boolean isToRead() {
        return _toRead;
    }
    private boolean _toRead = false;

    /**
     * Mark whether this object needs to be written.
     *
     * @param state true if the object needs to be written, false otherwise
     * @see AbstractValue
     */
    public void setToWrite(boolean state) {
        _toWrite = state;
    }

    /**
     * Ask whether this object needs to be written.
     *
     * @return true if the object needs to be written, false otherwise
     * @see AbstractValue
     */
    public boolean isToWrite() {
        return _toWrite;
    }
    private boolean _toWrite = false;

    /**
     * Gets the color associated with a particular state value.
     *
     * @param val a state value
     * @return the color assigned to the specified state value
     */
    public static Color stateColorFromValue(int val) {
        switch (val) {
            case UNKNOWN:
                return COLOR_UNKNOWN;
            case EDITED:
                return COLOR_EDITED;
            case READ:
                return COLOR_READ;
            case STORED:
                return COLOR_STORED;
            case FROMFILE:
                return COLOR_FROMFILE;
            case SAME:
                return COLOR_SAME;
            case DIFF:
                return COLOR_DIFF;
            default:
                return null;
        }
    }

    /**
     * Gets the name associated with a particular state value.
     *
     * @param val a state value
     * @return the name assigned to the specified state value
     */
    public static String stateNameFromValue(int val) {
        switch (val) {
            case UNKNOWN:
                return "Unknown";
            case EDITED:
                return "Edited";
            case READ:
                return "Read";
            case STORED:
                return "Stored";
            case FROMFILE:
                return "FromFile";
            case SAME:
                return "Same";
            case DIFF:
                return "Different";
            default:
                return "<unexpected value: " + val + ">";
        }
    }

    /**
     * Sets the availability status of the object.
     *
     * @param avail {@code true} if the object should be made available,
     *              {@code false} if should be made unavailable
     */
    public void setAvailable(boolean avail) {
        boolean oldval = this.available;
        available = avail;
        if (oldval != available) {
            prop.firePropertyChange("Available", Boolean.valueOf(oldval), Boolean.valueOf(avail));
        }
    }

    /**
     * Gets the current availability status of the object.
     * <p>
     * Code can use this to determine whether to set object visibility (or other
     * properties).
     *
     * @return {@code true} if the object should be available, {@code false} if
     *         it should be unavailable
     */
    public boolean getAvailable() {
        return available;
    }
    private boolean available = true;

    /**
     * @param p the listener to be added to the object
     */
    public void addPropertyChangeListener(PropertyChangeListener p) {
        prop.addPropertyChangeListener(p);
    }

    /**
     * @param p the listener to be removed from the object
     */
    public void removePropertyChangeListener(PropertyChangeListener p) {
        prop.removePropertyChangeListener(p);
    }
}

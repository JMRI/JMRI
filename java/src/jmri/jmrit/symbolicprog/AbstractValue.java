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
 * Represents a single CV value
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

    public enum ValueState {
        /**
         * Defines state when nothing is known about the real value.
         */
        UNKNOWN(COLOR_UNKNOWN, "Unknown"),

        /**
         * Defines state where value has been edited, no longer same as in decoder
         * or file.
         */
        EDITED(COLOR_EDITED, "Edited"),

        /**
         * Defines state where value has been read from (hence same as) decoder, but
         * perhaps not same as in file.
         */
        READ(COLOR_READ, "Read"),

        /**
         * Defines state where value has been written to (hence same as) decoder,
         * but perhaps not same as in file.
         */
        STORED(COLOR_STORED, "Stored"),

        /**
         * Defines state where value was read from a config file, but might not be
         * the same as the decoder.
         */
        FROMFILE(COLOR_FROMFILE, "FromFile"),

        /**
         * Defines state where value was read from a config file, and is the same as
         * the decoder.
         */
        SAME(COLOR_SAME, "Same"),

        /**
         * Defines state where value was read from a config file, and is the not the
         * same as the decoder.
         */
        DIFF(COLOR_DIFF, "Different");


        private final Color _color;
        private final String _name;

        private ValueState(Color color, String name) {
            this._color = color;
            this._name = name;
        }

        /**
         * Gets the color associated with this state value.
         * @return the color assigned to this state value
         */
        public Color getColor() {
            return _color;
        }

        /**
         * Gets the name associated with this state value.
         * @return the name assigned to this state value
         */
        public String getName() {
            return _name;
        }
    }

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

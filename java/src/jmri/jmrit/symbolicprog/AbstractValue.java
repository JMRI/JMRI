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
        UNKNOWN(Color.red.brighter(), "Unknown"),

        /**
         * Defines state where value has been edited, no longer same as in decoder
         * or file.
         */
        EDITED(Color.orange, "Edited"),

        /**
         * Defines state where value has been read from (hence same as) decoder, but
         * perhaps not same as in file.
         */
        READ(null, "Read"),

        /**
         * Defines state where value has been written to (hence same as) decoder,
         * but perhaps not same as in file.
         */
        STORED(null, "Stored"),

        /**
         * Defines state where value was read from a config file, but might not be
         * the same as the decoder.
         */
        FROMFILE(Color.yellow, "FromFile"),

        /**
         * Defines state where value was read from a config file, and is the same as
         * the decoder.
         */
        SAME(null, "Same"),

        /**
         * Defines state where value was read from a config file, and is the not the
         * same as the decoder.
         */
        DIFFERENT(Color.red.brighter(), "Different");


        private final Color _color;
        private final String _name;

        private ValueState(Color color, String name) {
            this._color = color;
            this._name = name;
        }

        /**
         * Gets the color associated with this state value.
         * @return the color assigned to this state value or null if
         *         to use default for the component
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

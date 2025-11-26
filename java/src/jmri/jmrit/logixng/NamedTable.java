package jmri.jmrit.logixng;

import java.beans.PropertyChangeEvent;

import jmri.NamedBean;

/**
 * Represent a named table.
 * A named table is a table that is a NamedBean.
 *
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public interface NamedTable extends Table, NamedBean {

    static class NamedTablePropertyChangeEvent extends PropertyChangeEvent {

        private final int _row;
        private final int _column;

        public NamedTablePropertyChangeEvent(
                Object source, String propertyName,
                Object oldValue, Object newValue,
                int row, int column) {
            super(source, propertyName, oldValue, newValue);
            this._row = row;
            this._column = column;
        }

        public int getRow() { return _row; }
        public int getColumn() { return _column; }
    }

    /**
     * This property tells that a cell in a LogixNG Table has been changed.
     * The property change event is of the type NamedTablePropertyChangeEvent.
     */
    public static final String PROPERTY_CELL_CHANGED = "CELL_CHANGED";

}

package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * Represent a named table.
 * A named table is a table that is a NamedBean.
 *
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public interface NamedTable extends Table, NamedBean {

    static class RowAndColumn {
        public int _row;
        public int _column;
    }

    public static final String PROPERTY_CELL_CHANGED = "CELL_CHANGED";

    static String getProperty(String desiredProperty, int row, int column) {
        return String.format("%s_%d_%d", desiredProperty, row, column);
    }

    static boolean isProperty(String desiredProperty, String actualProperty, RowAndColumn rowAndColumn) {
        if (actualProperty.startsWith(desiredProperty)) {
            String[] rowCol = actualProperty.substring(desiredProperty.length()+1).split("_");
            rowAndColumn._row = Integer.parseInt(rowCol[0]);
            rowAndColumn._column = Integer.parseInt(rowCol[1]);
            return true;
        }
        return false;
    }

}

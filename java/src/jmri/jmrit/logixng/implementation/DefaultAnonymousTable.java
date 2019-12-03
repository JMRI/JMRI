package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.AnonymousTable;

/**
 * Default implementation for anonymous tables
 */
public class DefaultAnonymousTable implements AnonymousTable {

    private final int _numRows;
    private final int _numColumns;
    private final Object[][] _data;
    
    public DefaultAnonymousTable(int numRows, int numColumns) {
        _numRows = numRows;
        _numColumns = numColumns;
        _data = new Object[numRows+1][numColumns+1];
    }
    
}

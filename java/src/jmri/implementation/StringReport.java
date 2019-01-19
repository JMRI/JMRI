package jmri.implementation;

import jmri.Report;

/**
 * A Report that has a String.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class StringReport implements Report {

    private final String _str;
    
    public StringReport(String str) {
        _str = str;
    }
    
    @Override
    public String getString() {
        return _str;
    }
    
    @Override
    public String toString() {
        return getString();
    }
    
}

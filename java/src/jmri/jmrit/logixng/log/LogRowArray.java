package jmri.jmrit.logixng.log;

/**
 * One row of log data stored in an array.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public final class LogRowArray implements LogRow {

    final long[] _row;
    final int _numStates;
    
    public LogRowArray(int numStates) {
        int sizeOfArray = numStates / BITS_PER_LONG;
        if ((numStates % BITS_PER_LONG) > 0) {
            sizeOfArray++;
        }
        _row = new long[sizeOfArray];
        _numStates = numStates;
    }
    
    public LogRowArray(long row[], int numStates) {
        _row = row.clone();
        _numStates = numStates;
    }
    
    public LogRowArray(String rowString) {
        int numStates = rowString.length();
        int sizeOfArray = numStates / BITS_PER_LONG;
        if ((numStates % BITS_PER_LONG) > 0) {
            sizeOfArray++;
        }
        _row = new long[sizeOfArray];
        char[] chars = rowString.toCharArray();
        for (int i=0; i < numStates; i++) {
            setState(i, chars[i] == '1');
        }
        _numStates = rowString.length();
    }
    
    /** {@inheritDoc} */
    @Override
    public int getNumStates() {
        return _numStates;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getState(int index) {
        int indexInArray = index / BITS_PER_LONG;
        int indexInLong = index % BITS_PER_LONG;
        
        return ((_row[indexInArray] >> indexInLong) & 0x01) == 1;
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int index, boolean state) {
        int indexInArray = index / BITS_PER_LONG;
        int indexInLong = index % BITS_PER_LONG;
        
        long bit = (state ? 1 : 0) << indexInLong;
        
        _row[indexInArray] |= bit;
    }
    
    /** {@inheritDoc} */
    @Override
    public long[] getData() {
        return _row.clone();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDataString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < _numStates; i++) {
            sb.append(getState(i) ? '1' : '0');
        }
        return sb.toString();
    }
    
}

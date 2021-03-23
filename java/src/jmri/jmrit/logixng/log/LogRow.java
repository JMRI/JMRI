package jmri.jmrit.logixng.log;

/**
 * The states of the items in one row in the log.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogRow {

    /**
     * The number of bits used in each long in the array returned by getData().
     */
    public final int BITS_PER_LONG = 64;
    
    /**
     * Get the number of states in the row.
     * @return the number of states in the row
     */
    public int getNumStates();
    
    /**
     * Get the state of one item.
     * 
     * @param index the row index
     * @return true if the state is active
     */
    public boolean getState(int index);
    
    /**
     * Get the state of one item.
     * 
     * @param index the row index
     * @param state the new state
     */
    public void setState(int index, boolean state);
    
    /**
     * Get a copy of the data as an array of long.
     * This could be, but don't need to be, the way data is stored
     * internally.
     * 
     * @return a clone of the raw data
     */
    public long[] getData();
    
    /**
     * Get data as a string of 1's and 0's
     * @return the data
     */
    public String getDataString();
    
}

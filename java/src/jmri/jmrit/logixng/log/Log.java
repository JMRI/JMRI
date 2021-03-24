package jmri.jmrit.logixng.log;

import java.util.List;

/**
 * A LogixNG log.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface Log {

    /**
     * The log has an invalid format.
     */
    public static class InvalidFormatException extends Exception {
        public InvalidFormatException(String message) {
            super(message);
        }
    }
    
    /**
     * The version in the log file is not supported.
     */
    public static class UnsupportedVersionException extends Exception {
        public UnsupportedVersionException(String message) {
            super(message);
        }
    }
    
    /**
     * Add an item to the log.
     * All items in the log must be added before logging is started. New items
     * must not be added once logging is active.
     * 
     * @param systemName the system name of the item
     * @return the index of the item in the log
     */
    public int addItem(String systemName);
    
    /**
     * Get the index of an item in the log.
     * 
     * @param systemName the system name of the item
     * @return the index of the item in the log
     * @throws IllegalArgumentException if the item is not in the log
     */
    public int getItemIndex(String systemName) throws IllegalArgumentException;
    
    /**
     * Clear the list of items.
     */
    public void clearItemList();
    
    /**
     * Get the list of items.
     * @return the list of items
     */
    public List<String> getItemList();
    
    /**
     * Get the number of items in the log.
     * @return the number of states in the log
     */
    public int getNumItems();
    
}

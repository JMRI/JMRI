package jmri.jmrix.bidib;

/**
 * This interface specifies methods that the BiDiB object classes (turnouts, sensors, lights, reporters, signal masts) must implement.
 * 
 * @author Eckart Meyer Copyright (C) 2020-2023
 */
public interface BiDiBNamedBeanInterface {
    
    /**
     * Get the BiDiB address instance
     * 
     * @return BiDiBAddress
     */
    public BiDiBAddress getAddr();
    
    /**
     * Helper function that will be invoked after construction once the type has been
     * set. Used specifically for preventing double initialization when loading turnouts from XML.
     */
    public default void finishLoad() {}
    
    /**
     * called then a new node has been discovered
     */
    public void nodeNew();

    /**
     * called then a node was lost
     */
    public void nodeLost();
    
}

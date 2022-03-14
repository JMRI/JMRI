package jmri.jmrix.can.cbus.swing.modules;

/**
 * Interface for callback function(s) used to update the NVs
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public interface UpdateNV {

    /**
     * Build a new NV value from contents of gui elements
     * 
     * @param index Index of something. e.g., an NV, or an output bit, etc
     */
    void setNewVal(int index);
}

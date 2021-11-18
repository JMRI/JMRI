package jmri;

import jmri.PhysicalLocationReporter.Direction;

/**
 * Extended report.
 * <P>
 * THIS CLASS IS CURRENTLY EXPERIMENTAL AND SUBJECT TO CHANGES.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 * @see jmri.Reporter
 * @since 4.25.6
 */
public interface ExtendedReport extends Reportable {

    public static final ExtendedReport NULL_REPORT = new NullReport();
    
    
    /**
     * Does this report has a loco address?
     * @return true if the report has a loco address, false otherwise.
     */
    public default boolean hasLocoAddress() {
        return false;
    }

    /**
     * Get the loco address.
     * @return the loco address
     * @throws UnsupportedOperationException if hasLocoAddress() returns false
     */
    public default LocoAddress getLocoAddress() {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Does this report has a direction?
     * @return true if the report has a direction, false otherwise.
     */
    public default boolean hasDirection() {
        return false;
    }

    /**
     * Get the direction.
     * @return the direction
     * @throws UnsupportedOperationException if hasDirection() returns false
     */
    public default Direction getDirection() {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Does this report has a physical location?
     * @return true if the report has a physical location, false otherwise.
     */
    public default boolean hasPhysicalLocation() {
        return false;
    }

    /**
     * Get the physical location.
     * @return the physical location
     * @throws UnsupportedOperationException if hasPhysicalLocation()
     *         returns false
     */
    public default PhysicalLocation getPhysicalLocation() {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Does this report has a region id?
     * A region id is a string that is an unique identifier for a region
     * on the layout.
     * @return true if the report has a region id, false otherwise.
     */
    public default boolean hasRegionId() {
        return false;
    }

    /**
     * Get the region id.
     * A region id is a string that is an unique identifier for a region
     * on the layout.
     * @return the region id
     * @throws UnsupportedOperationException if hasRegionId() returns false
     */
    public default String getRegionId() {
        throw new UnsupportedOperationException("Not supported");
    }



    /**
     * A null report.
     * This class must not be used by the JMRI project. It's to be used by
     * end users who call Reporter.setReport() from scripts and now must call
     * Reporter.setExtendedReport() instead, but has no interest in extended
     * reports.
     * <p>
     * This class might also be used by third party software that uses JMRI as
     * a library.
     */
    static class NullReport implements ExtendedReport {
        
        // Private constructor to protect from being created outside of this class.
        private NullReport() {}
    }

}

package jmri;

import jmri.util.PhysicalLocation;

/**
 * Extended report.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 * @see jmri.Reporter
 * @since 4.25.6
 */
public interface ExtendedReport extends Reportable {

    public default boolean hasLocoAddress() {
        return false;
    }

    public default LocoAddress getLocoAddress() {
        throw new UnsupportedOperationException("Not supported");
    }

    public default boolean hasPhysicalLocation() {
        return false;
    }

    public default PhysicalLocation getPhysicalLocation() {
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
    public static class NullReport implements ExtendedReport {
        
    }

}

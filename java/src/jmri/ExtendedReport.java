package jmri;

/**
 * Extended report.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 * @see jmri.Reporter
 * @since 4.25.4
 */
public interface ExtendedReport extends Reportable {












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

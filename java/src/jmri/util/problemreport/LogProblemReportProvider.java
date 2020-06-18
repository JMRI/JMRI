package jmri.util.problemreport;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Provider for logging-related information in a problem report.
 *
 * @author Randall Wood Copyright 2020
 */
public interface LogProblemReportProvider {

    /**
     * Get log files to be appended to report.
     *
     * @return the logs to append
     */
    @Nonnull
    public File[] getFiles();
}

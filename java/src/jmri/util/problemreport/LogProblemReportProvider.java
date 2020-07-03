package jmri.util.problemreport;

import java.io.File;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Provider for logging-related information in a problem report.
 *
 * @author Randall Wood Copyright 2020
 */
@API(status=API.Status.EXPERIMENTAL)
public interface LogProblemReportProvider {

    /**
     * Get log files to be appended to report.
     *
     * @return the logs to append
     */
    @Nonnull
    public File[] getFiles();
}

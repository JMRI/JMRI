package apps.util.issuereporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import jmri.util.FileUtil;
import jmri.util.startup.StartupRunnable;

import org.apiguardian.api.API;
import org.openide.util.lookup.ServiceProvider;

/**
 * Remove remnants of prior issue reports.
 * 
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.INTERNAL)
@ServiceProvider(service = StartupRunnable.class)
public final class IssueReporterStartupRunnable implements StartupRunnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueReporterStartupRunnable.class);

    @Override
    public void run() {
            Path tempDir = new File(System.getProperty("java.io.tmpdir")).toPath();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir, entry -> entry.toFile().getName().startsWith("jmri-issue-report-"))) {
                    stream.forEach(entry -> FileUtil.delete(entry.toFile()));
        } catch (IOException ex) {
            log.error("Exception cleaning up from issue reporter", ex);
        }
    }
}

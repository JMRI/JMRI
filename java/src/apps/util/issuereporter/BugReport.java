package apps.util.issuereporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.prefs.InitializationException;

import org.jdom2.JDOMException;

/**
 *
 * @author rhwood
 */
public class BugReport extends IssueReport {

    private boolean includeProfile;
    private final boolean includeSysInfo;
    private final boolean includeLogs;
    private final List<File> files = new ArrayList<>();

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BugReport.class);

    public BugReport(String title, String body, boolean includeProfile, boolean includeSysInfo, boolean includeLogs) {
        super(title, body);
        this.includeProfile = includeProfile;
        this.includeSysInfo = includeSysInfo;
        this.includeLogs = includeLogs;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
                justification="Null check in stream.forEach() is embedded, beyond our control")
    public void prepare() {
        title = Bundle.getMessage(Locale.US, "bug.title", title);
        if (!tooLong) {
            try {
                Path dir = Files.createTempDirectory("jmri-issue-report-" + new Date().getTime());
                if (includeLogs) {
                    Files.copy(new File(System.getProperty("jmri.log.path"), "session.log").toPath(), dir.resolve("session.log"), StandardCopyOption.REPLACE_EXISTING);
                }
                if (includeProfile) {
                    Profile profile = ProfileManager.getDefault().getActiveProfile();
                    if (profile != null) {
                        File archive = new File(dir.toFile(), "profile.zip");
                        ProfileManager.getDefault().export(profile, archive, false, false);
                    }
                }
                if (includeSysInfo) {
                    getSysInfoFile(dir);
                }
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    stream.forEach(p -> files.add(p.toFile()));
                }
            } catch (IOException | JDOMException | InitializationException | NullPointerException ex) {
                log.error("Unable to include profile in report.", ex);
                includeProfile = false;
            }
            body = Bundle.getMessage(Locale.US,
                    "bug.body",
                    !files.isEmpty() ? Bundle.getMessage("instructions.paste.files") : "",
                    body,
                    getSimpleContext(),
                    getIssueFooter());
        } else {
            body = Bundle.getMessage("instructions.paste.414");
        }
    }

    private void getSysInfoFile(Path path) {
        Path file = path.resolve("systemInfo.txt");
        try {
            Files.createFile(file);
            Files.write(file, new SystemInfo(false).asList(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            log.error("Exception writing system info", ex);
        }
    }

    @Override
    public List<File> getAttachments() {
        return files;
    }
}

package apps.util.issuereporter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.profile.ProfileManager;
import jmri.util.prefs.InitializationException;

import org.apache.commons.io.FileUtils;
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
    public void prepare() {
        String instructions = "";
        title = Bundle.getMessage(Locale.US, "bug.title", title);
        if (!tooLong) {
            if (includeProfile) {
                try {
                    Path dir = Files.createTempDirectory("jmri-issue-report-" + new Date().getTime());
                    Files.copy(new File(System.getProperty("jmri.log.path"), "session.log").toPath(), dir.resolve("session.log"), StandardCopyOption.REPLACE_EXISTING);
                    java.awt.Desktop.getDesktop().open(dir.toFile());
                    File archive = new File(dir.toFile(), "profile.zip");
                    ProfileManager.getDefault().export(ProfileManager.getDefault().getActiveProfile(), archive, false, false);
                    Files.newDirectoryStream(dir).forEach(p -> files.add(p.toFile()));
                    // TODO: copy sysInfo to temp file and add to files
                } catch (IOException | JDOMException | InitializationException | NullPointerException ex) {
                    log.error("Unable to include profile in report.", ex);
                    includeProfile = false;
                }
            }
            body = Bundle.getMessage(Locale.US,
                    "bug.body",
                    instructions,
                    body,
                    getSimpleContext(),
                    !includeProfile ? getSysInfo() : "",
                    !includeProfile ? getLogs() : "",
                    !files.isEmpty() ? Bundle.getMessage("instructions.paste.files") : "",
                    getIssueFooter());
        } else {
            body = Bundle.getMessage("instructions.paste.414");
        }
    }

    private String getLogs() {
        try {
            return includeLogs
                    ? "<details>\n<summary>Session Log</summary>\n\n```\n"
                    + FileUtils.readFileToString(new File(System.getProperty("jmri.log.path"), "session.log"), StandardCharsets.UTF_8)
                    + "```\n</details>\n\n"
                    : "";
        } catch (IOException ex) {
            log.error("Exception reading session log", ex);
            return "";
        }
    }

    @Nonnull
    private String getSysInfo() {
        return includeSysInfo ? new SystemInfo(true).toString() : "";
    }
    
    @Override
    public List<File> getAttachments() {
        return files;
    }
}

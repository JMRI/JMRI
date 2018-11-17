package jmri.util.junit.rules;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.rules.TemporaryFolder;

/**
 * Extends {@link junit.rules.TemporaryFolder} to ensure that the test directory
 * is unique.
 *
 * @author Bob Jacobsen 2018
 */

public class JmriTemporaryFolder extends TemporaryFolder {

    static private File makeFolder() {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            File result = Files.createTempDirectory("jmri", attr).toFile();
            result.deleteOnExit();
            return result;
        } catch (IOException e) {
            log.error("Could not create temporary file", e);
            return null;
        }
    }
    
    public JmriTemporaryFolder() {
        super(makeFolder());
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriTemporaryFolder.class);
}

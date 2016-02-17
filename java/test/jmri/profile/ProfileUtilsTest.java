package jmri.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link jmri.beans.Beans} static methods.
 *
 * @author Randall Wood
 */
public class ProfileUtilsTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(ProfileUtilsTest.class);

    public ProfileUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCopy() {
        Profile source;
        Profile destination;
        try {
            File dir = Files.createTempDirectory("source").toFile();
            source = new Profile("source", dir.getName(), dir);
            dir = Files.createTempDirectory("dest").toFile();
            destination = new Profile("destination", dir.getName(), dir);
        } catch (IOException ex) {
            // skip test if unable to create temporary profiles
            TestCase.fail("Unable to create temporary profiles");
            return;
        }
        try {
            ProfileUtils.copy(source, destination);
        } catch (IllegalArgumentException ex) {
            TestCase.fail("Unable to copy profiles with IllegalArgumentException.");
            return;
        } catch (IOException ex) {
            TestCase.fail("Unable to copy profiles with IOException.");
            return;
        }
        File profile = new File(destination.getPath(), Profile.PROFILE);
        for (File file : profile.listFiles((File pathname) -> (pathname.getName().endsWith(source.getUniqueId())))) {
            TestCase.fail("Source ID remains in destination profile.");
        }
    }

    public void testCopyToActive() {
        Profile source;
        Profile destination;
        try {
            File dir = Files.createTempDirectory("source").toFile();
            source = new Profile("source", dir.getName(), dir);
            dir = Files.createTempDirectory("dest").toFile();
            destination = new Profile("destination", dir.getName(), dir);
            // Should cause copy() to throw IllegalArgumentException
            ProfileManager.getDefault().setActiveProfile(destination);
        } catch (IOException ex) {
            // skip test if unable to create temporary profiles
            TestCase.fail("Unable to create temporary profiles");
            return;
        }
        try {
            // Should throw IllegalArgumentException
            ProfileUtils.copy(source, destination);
        } catch (IllegalArgumentException ex) {
            // This exception is expected and throwing it passes the test
            return;
        } catch (IOException ex) {
            TestCase.fail("Unable to copy profiles with IOException.");
            return;
        }
    }

}

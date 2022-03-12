package jmri.profile;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link jmri.profile.ProfileUtils} methods.
 *
 * @author Randall Wood
 */
public class ProfileUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(ProfileUtilsTest.class);

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.tearDown();
    }

    @Test
    public void testCopy(@TempDir File folder) {
        Profile source;
        Profile destination;
        try {
            File dir = new File(folder, "source");
            dir.mkdirs();
            source = new Profile("source", dir.getName(), dir);
            dir = new File(folder, "dest");
            destination = new Profile("destination", dir.getName(), dir);
        } catch (IOException ex) {
            // skip test if unable to create temporary profiles
            fail("Unable to create temporary profiles");
            return;
        }
        try {
            ProfileUtils.copy(source, destination);
        } catch (IllegalArgumentException ex) {
            fail("Unable to copy profiles with IllegalArgumentException.");
            return;
        } catch (IOException ex) {
            log.error("Failure copying profiles", ex);
            fail("Unable to copy profiles with IOException.");
            return;
        }
        File profile = new File(destination.getPath(), Profile.PROFILE);
        if (profile.listFiles((File pathname) -> (pathname.getName().endsWith(source.getUniqueId()))).length > 0) {
            fail("Source ID remains in destination profile.");
        }
    }

    @Test
    public void testCopyToActive(@TempDir File folder) {
        Profile source;
        Profile destination;
        try {
            File dir = new File(folder, "source");
            dir.mkdirs();
            source = new Profile("source", dir.getName(), dir);
            dir = new File(folder, "dest");
            dir.mkdirs();
            destination = new Profile("destination", dir.getName(), dir);
            // Should cause copy() to throw IllegalArgumentException
            ProfileManager.getDefault().setActiveProfile(destination);
        } catch (IOException ex) {
            // skip test if unable to create temporary profiles
            fail("Unable to create temporary profiles");
            return;
        }
        try {
            // Should throw IllegalArgumentException
            ProfileUtils.copy(source, destination);
        } catch (IllegalArgumentException ex) {
            // This exception is expected and throwing it passes the test
        } catch (IOException ex) {
            log.error("Failure copying profiles", ex);
            fail("Unable to copy profiles with IOException.");
        }
    }

}

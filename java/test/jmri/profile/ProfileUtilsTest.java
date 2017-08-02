package jmri.profile;

import static org.junit.Assert.fail;

import apps.tests.Log4JFixture;
import java.io.File;
import java.io.IOException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link jmri.beans.Beans} static methods.
 *
 * @author Randall Wood
 */
public class ProfileUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(ProfileUtilsTest.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetProfileManager();
        Log4JFixture.tearDown();
    }

    @Test
    public void testCopy() {
        Profile source;
        Profile destination;
        try {
            File dir = folder.newFolder("source");
            source = new Profile("source", dir.getName(), dir);
            dir = folder.newFolder("dest");
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
    public void testCopyToActive() {
        Profile source;
        Profile destination;
        try {
            File dir = folder.newFolder("source");
            source = new Profile("source", dir.getName(), dir);
            dir = folder.newFolder("dest");
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

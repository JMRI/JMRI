package jmri.profile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link jmri.profile.ProfileUtils} methods.
 *
 * @author Randall Wood
 */
public class ProfileUtilsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetProfileManager();
        JUnitUtil.tearDown();
    }

    @Test
    public void testCopy(@TempDir File folder) {
        // Profile source;
        Profile source = assertDoesNotThrow( () -> {
            File dir = new File(folder, "source");
            assertTrue(dir.mkdirs());
            return new Profile("source", dir.getName(), dir);
        }, "Unable to create source profile");
        Profile destination = assertDoesNotThrow( () -> {
            File dir = new File(folder, "dest");
            return new Profile("destination", dir.getName(), dir);
        }, "Unable to create temporary destination profile");

        assertDoesNotThrow( () ->
            ProfileUtils.copy(source, destination),
            "Unable to copy profiles.");

        File profile = new File(destination.getPath(), Profile.PROFILE);
        File[] profilePaths = profile.listFiles((File pathname) ->
            pathname.getName().endsWith(source.getUniqueId()));
        assertNotNull(profilePaths);
        if ( profilePaths.length > 0 ) { 
            fail("Source ID remains in destination profile.");
        }
    }

    @Test
    public void testCopyToActive(@TempDir File folder) {
        
        Profile source = assertDoesNotThrow( () -> {
            File dir = new File(folder, "source");
            assertTrue(dir.mkdirs());
            return new Profile("source", dir.getName(), dir);
                    }, "Unable to create temporary source profile");
        Profile destination = assertDoesNotThrow( () -> {
            File dir = new File(folder, "dest");
            assertTrue(dir.mkdirs());
            return new Profile("destination", dir.getName(), dir);
        }, "Unable to create temporary dest profile");
        // Should cause copy() to throw IllegalArgumentException
        ProfileManager.getDefault().setActiveProfile(destination);

        // Should throw IllegalArgumentException
        // This exception is expected and throwing it passes the test
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> ProfileUtils.copy(source, destination), "Unable to copy profiles.");
        assertNotNull(ex);

    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfileUtilsTest.class);

}

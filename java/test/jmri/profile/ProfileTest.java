package jmri.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Randall Wood
 */
public class ProfileTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of constructor with extension for Profile path.
     *
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs.
     */
    @Test
    public void testProfileWithExtension(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        assertEquals( "test", instance.getName(), "Name has no extension");
        assertEquals( "test" + Profile.EXTENSION, instance.getPath().getName(),
            "Path name has extension");
    }

    /**
     * Test of save method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException on any unanticipated errors setting up test
     */
    @Test
    public void testSave(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("saved");
        instance.save();
        assertEquals("saved",
            new ProfileProperties(instance.getPath()).get(Profile.NAME, true));
    }

    /**
     * Test of getName method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetName(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        assertEquals("test", instance.getName());
    }

    /**
     * Test of setName method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testSetName(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("changed");
        assertEquals("changed", instance.getName());
    }

    /**
     * Test of getId method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetId(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        String id = new ProfileProperties(instance.getPath()).get(Profile.ID, true);
        assertEquals(id, instance.getId());
    }

    /**
     * Test of getPath method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetPath(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        File profileExtFolder = new File(profileFolder.getParentFile(), "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        assertNotEquals(profileFolder, instance.getPath());
        assertEquals(profileExtFolder, instance.getPath());
    }

    /**
     * Test of toString method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testToString(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        assertEquals(instance.getName(), instance.toString());
    }

    /**
     * Test of hashCode method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testHashCode(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
        assertEquals(71 * 7 + id.hashCode(), instance.hashCode());
    }

    /**
     * Test of equals method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    // tests that equals() does not allow a String to equal a Profile
    @SuppressWarnings({"unlikely-arg-type", "IncompatibleEquals"})
    public void testEquals(@TempDir File folder) throws IOException {
        File rootFolder = folder;
        File profileFolder = new File(rootFolder, "test");
        File profileFolder2 = new File(rootFolder, "test2");
        File profileFolder3 = new File(rootFolder, "test3");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.save();
        Profile instance2 = new Profile("test", "test2", profileFolder2);
        instance2.save();
        FileUtil.copy(instance.getPath(), profileFolder3);
        Profile instance3 = new Profile(profileFolder3);
        assertNotNull(instance);
        assertFalse(instance.equals(""));
        assertFalse(instance.equals(instance2));
        assertTrue(instance.equals(instance3));
    }

    /**
     * Test of isComplete method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testIsComplete(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        assertTrue(instance.isComplete());
    }

    /**
     * Test of getUniqueId method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetUniqueId(@TempDir File folder) throws IOException {
        File profileFolder = new File(folder, "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
        id = id.substring(id.lastIndexOf('.') + 1);
        assertEquals(id, instance.getUniqueId());
    }

    /**
     * Test of containsProfile method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testContainsProfile(@TempDir File folder) throws IOException {
        File rootFolder = new File(folder, Profile.PROFILE);
        File profileFolder = new File(rootFolder, "test");
        assertTrue(profileFolder.mkdirs());
        File rootFolder2 = new File(folder, Profile.PATH);
        assertTrue( new File(rootFolder2, "test2").mkdirs());
        assertNotNull(new Profile("test", "test", profileFolder));
        assertTrue(Profile.containsProfile(rootFolder));
        assertFalse(Profile.containsProfile(rootFolder2));
    }

    /**
     * Test of inProfile method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testInProfile(@TempDir File folder) throws IOException {
        File rootFolder = folder;
        File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
        File innerFolder = new File(profileFolder, "test");
        assertTrue(innerFolder.mkdirs());
        File rootFolder2 = new File(folder, Profile.PATH);
        assertTrue(rootFolder2.mkdirs());
        assertNotNull(new Profile("test", "test", profileFolder));
        assertTrue(Profile.inProfile(innerFolder));
        assertFalse(Profile.inProfile(rootFolder2));
    }

    /**
     * Test of isProfile method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testIsProfile(@TempDir File folder) throws IOException {
        File rootFolder = folder;
        File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
        assertNotNull(new Profile("test", "test", profileFolder));
        File innerFolder = new File(profileFolder, "test");
        assertTrue(innerFolder.mkdirs());
        assertTrue(Profile.isProfile(profileFolder));
        assertFalse(Profile.isProfile(rootFolder));
        assertFalse(Profile.isProfile(innerFolder));
    }

    /**
     * Test of compareTo method, of class Profile.
     * @param folder automatically inserted via JUnit.
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testCompareTo(@TempDir File folder) throws IOException {
        File rootFolder = folder;
        File profileFolder = new File(rootFolder, "test");
        File profileFolder2 = new File(rootFolder, "test2");
        File profileFolder3 = new File(rootFolder, "test3");
        Profile instance = new Profile("test", "test", profileFolder);
        Profile instance2 = new Profile("test", "test2", profileFolder2);
        FileUtil.copy(instance.getPath(), profileFolder3);
        Profile instance3 = new Profile(profileFolder3);
        // the contract for .compareTo is to return <= -1, 0, >= 1
        assertTrue(-1 >= instance.compareTo(instance2));
        assertEquals(0, instance.compareTo(instance3));
        assertTrue(1 <= instance2.compareTo(instance));
    }

}

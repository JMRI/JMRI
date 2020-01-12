package jmri.profile;

import java.io.File;
import java.io.IOException;
import jmri.util.FileUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Randall Wood
 */
public class ProfileTest {

    public ProfileTest() {
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterClass
    public static void tearDownClass() {
        jmri.util.JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProfileWithExtension() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
            Profile instance = new Profile("test", "test", profileFolder);
            Assert.assertEquals("Name has no extension", "test", instance.getName());
            Assert.assertEquals("Path name has extension", "test" + Profile.EXTENSION, instance.getPath().getName());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    /**
     * Test of save method, of class Profile.
     *
     * @throws java.io.IOException on any unanticipated errors setting up test
     */
    @Test
    public void testSave() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("saved");
        instance.save();
        Assert.assertEquals("saved", (new ProfileProperties(instance.getPath())).get(Profile.NAME, true));
    }

    /**
     * Test of getName method, of class Profile.
     */
    @Test
    public void testGetName() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            Profile instance = new Profile("test", "test", profileFolder);
            Assert.assertEquals("test", instance.getName());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of setName method, of class Profile.
     */
    @Test
    public void testSetName() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            Profile instance = new Profile("test", "test", profileFolder);
            instance.setName("changed");
            Assert.assertEquals("changed", instance.getName());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of getId method, of class Profile.
     */
    @Test
    public void testGetId() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            Profile instance = new Profile("test", "test", profileFolder);
            String id = (new ProfileProperties(instance.getPath())).get(Profile.ID, true);
            Assert.assertEquals(id, instance.getId());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of getPath method, of class Profile.
     */
    @Test
    public void testGetPath() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            File profileExtFolder = new File(profileFolder.getParentFile(), "test" + Profile.EXTENSION);
            Profile instance = new Profile("test", "test", profileFolder);
            Assert.assertNotEquals(profileFolder, instance.getPath());
            Assert.assertEquals(profileExtFolder, instance.getPath());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of toString method, of class Profile.
     */
    @Test
    public void testToString() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            Profile instance = new Profile("test", "test", profileFolder);
            Assert.assertEquals(instance.getName(), instance.toString());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of hashCode method, of class Profile.
     */
    @Test
    public void testHashCode() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
            Profile instance = new Profile("test", "test", profileFolder);
            String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
            Assert.assertEquals(71 * 7 + id.hashCode(), instance.hashCode());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of equals method, of class Profile.
     */
    @Test
    @SuppressWarnings("unlikely-arg-type") // String seems to be unrelated to Profile
    public void testEquals() {
        try {
            File rootFolder = folder.newFolder(Profile.PROFILE);
            File profileFolder = new File(rootFolder, "test");
            File profileFolder2 = new File(rootFolder, "test2");
            File profileFolder3 = new File(rootFolder, "test3");
            Profile instance = new Profile("test", "test", profileFolder);
            Profile instance2 = new Profile("test", "test2", profileFolder2);
            FileUtil.copy(instance.getPath(), profileFolder3);
            Profile instance3 = new Profile(profileFolder3);
            Assert.assertFalse(instance.equals(null));
            Assert.assertFalse(instance.equals(new String()));
            Assert.assertFalse(instance.equals(instance2));
            Assert.assertTrue(instance.equals(instance3));
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of isComplete method, of class Profile.
     */
    @Test
    public void testIsComplete() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
            Profile instance = new Profile("test", "test", profileFolder);
            Assert.assertTrue(instance.isComplete());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of getUniqueId method, of class Profile.
     */
    @Test
    public void testGetUniqueId() {
        try {
            File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
            Profile instance = new Profile("test", "test", profileFolder);
            String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
            id = id.substring(id.lastIndexOf(".") + 1);
            Assert.assertEquals(id, instance.getUniqueId());
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of containsProfile method, of class Profile.
     */
    @Test
    public void testContainsProfile() {
        try {
            File rootFolder = folder.newFolder(Profile.PROFILE);
            File profileFolder = new File(rootFolder, "test");
            File rootFolder2 = folder.newFolder(Profile.PATH);
            (new File(rootFolder2, "test2")).mkdirs();
            new Profile("test", "test", profileFolder);
            Assert.assertTrue(Profile.containsProfile(rootFolder));
            Assert.assertFalse(Profile.containsProfile(rootFolder2));
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of inProfile method, of class Profile.
     */
    @Test
    public void testInProfile() {
        try {
            File rootFolder = folder.newFolder(Profile.PROFILE);
            File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
            File innerFolder = new File(profileFolder, "test");
            innerFolder.mkdirs();
            File rootFolder2 = folder.newFolder(Profile.PATH);
            new Profile("test", "test", profileFolder);
            Assert.assertTrue(Profile.inProfile(innerFolder));
            Assert.assertFalse(Profile.inProfile(rootFolder2));
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of isProfile method, of class Profile.
     */
    @Test
    public void testIsProfile() {
        try {
            File rootFolder = folder.newFolder(Profile.PROFILE);
            File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
            new Profile("test", "test", profileFolder);
            File innerFolder = new File(profileFolder, "test");
            innerFolder.mkdirs();
            Assert.assertTrue(Profile.isProfile(profileFolder));
            Assert.assertFalse(Profile.isProfile(rootFolder));
            Assert.assertFalse(Profile.isProfile(innerFolder));
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test of compareTo method, of class Profile.
     */
    @Test
    public void testCompareTo() {
        try {
            File rootFolder = folder.newFolder(Profile.PROFILE);
            File profileFolder = new File(rootFolder, "test");
            File profileFolder2 = new File(rootFolder, "test2");
            File profileFolder3 = new File(rootFolder, "test3");
            Profile instance = new Profile("test", "test", profileFolder);
            Profile instance2 = new Profile("test", "test2", profileFolder2);
            FileUtil.copy(instance.getPath(), profileFolder3);
            Profile instance3 = new Profile(profileFolder3);
            // the contract for .compareTo is to return <= -1, 0, >= 1
            Assert.assertTrue(-1 >= instance.compareTo(instance2));
            Assert.assertEquals(0, instance.compareTo(instance3));
            Assert.assertTrue(1 <= instance2.compareTo(instance));
        } catch (IOException | IllegalArgumentException ex) {
            Assert.fail(ex.toString());
        }
    }

}

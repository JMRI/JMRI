package jmri.profile;

import java.io.File;
import java.io.IOException;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Randall Wood
 */
public class ProfileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of constructor with extension for Profile path.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testProfileWithExtension() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        Assert.assertEquals("Name has no extension", "test", instance.getName());
        Assert.assertEquals("Path name has extension", "test" + Profile.EXTENSION, instance.getPath().getName());
    }

    /**
     * Test of save method, of class Profile.
     *
     * @throws IOException on any unanticipated errors setting up test
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
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetName() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        Assert.assertEquals("test", instance.getName());
    }

    /**
     * Test of setName method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testSetName() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("changed");
        Assert.assertEquals("changed", instance.getName());
    }

    /**
     * Test of getId method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetId() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        String id = (new ProfileProperties(instance.getPath())).get(Profile.ID, true);
        Assert.assertEquals(id, instance.getId());
    }

    /**
     * Test of getPath method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetPath() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        File profileExtFolder = new File(profileFolder.getParentFile(), "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        Assert.assertNotEquals(profileFolder, instance.getPath());
        Assert.assertEquals(profileExtFolder, instance.getPath());
    }

    /**
     * Test of toString method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testToString() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        Assert.assertEquals(instance.getName(), instance.toString());
    }

    /**
     * Test of hashCode method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testHashCode() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
        Assert.assertEquals(71 * 7 + id.hashCode(), instance.hashCode());
    }

    /**
     * Test of equals method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    // tests that equals() does not allow a String to equal a Profile
    @SuppressWarnings("unlikely-arg-type")
    public void testEquals() throws IOException {
        File rootFolder = folder.newFolder(Profile.PROFILE);
        File profileFolder = new File(rootFolder, "test");
        File profileFolder2 = new File(rootFolder, "test2");
        File profileFolder3 = new File(rootFolder, "test3");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.save();
        Profile instance2 = new Profile("test", "test2", profileFolder2);
        instance2.save();
        FileUtil.copy(instance.getPath(), profileFolder3);
        Profile instance3 = new Profile(profileFolder3);
        Assert.assertFalse(instance.equals(null));
        Assert.assertFalse(instance.equals(new String()));
        Assert.assertFalse(instance.equals(instance2));
        System.out.println("Should be the same:");
        Assert.assertTrue(instance.equals(instance3));
    }

    /**
     * Test of isComplete method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testIsComplete() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test");
        Profile instance = new Profile("test", "test", profileFolder);
        Assert.assertTrue(instance.isComplete());
    }

    /**
     * Test of getUniqueId method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testGetUniqueId() throws IOException {
        File profileFolder = new File(folder.newFolder(Profile.PROFILE), "test" + Profile.EXTENSION);
        Profile instance = new Profile("test", "test", profileFolder);
        String id = (new ProfileProperties(profileFolder)).get(Profile.ID, true);
        id = id.substring(id.lastIndexOf(".") + 1);
        Assert.assertEquals(id, instance.getUniqueId());
    }

    /**
     * Test of containsProfile method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testContainsProfile() throws IOException {
        File rootFolder = folder.newFolder(Profile.PROFILE);
        File profileFolder = new File(rootFolder, "test");
        File rootFolder2 = folder.newFolder(Profile.PATH);
        (new File(rootFolder2, "test2")).mkdirs();
        new Profile("test", "test", profileFolder);
        Assert.assertTrue(Profile.containsProfile(rootFolder));
        Assert.assertFalse(Profile.containsProfile(rootFolder2));
    }

    /**
     * Test of inProfile method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testInProfile() throws IOException {
        File rootFolder = folder.newFolder(Profile.PROFILE);
        File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
        File innerFolder = new File(profileFolder, "test");
        innerFolder.mkdirs();
        File rootFolder2 = folder.newFolder(Profile.PATH);
        new Profile("test", "test", profileFolder);
        Assert.assertTrue(Profile.inProfile(innerFolder));
        Assert.assertFalse(Profile.inProfile(rootFolder2));
    }

    /**
     * Test of isProfile method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testIsProfile() throws IOException {
        File rootFolder = folder.newFolder(Profile.PROFILE);
        File profileFolder = new File(rootFolder, "test" + Profile.EXTENSION);
        new Profile("test", "test", profileFolder);
        File innerFolder = new File(profileFolder, "test");
        innerFolder.mkdirs();
        Assert.assertTrue(Profile.isProfile(profileFolder));
        Assert.assertFalse(Profile.isProfile(rootFolder));
        Assert.assertFalse(Profile.isProfile(innerFolder));
    }

    /**
     * Test of compareTo method, of class Profile.
     * 
     * @throws IOException if unexpected in context of test error occurs
     */
    @Test
    public void testCompareTo() throws IOException {
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
    }

}

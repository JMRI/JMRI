package jmri.util.prefs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Randall Wood 2015
 */
public class JmriPreferencesProviderTest extends TestCase {
    
    private Path workspace;
    
    public JmriPreferencesProviderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workspace = Files.createTempDirectory(this.getName());
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtil.delete(this.workspace.toFile());
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriPreferencesProviderTest.class);
        return suite;
    }

    /**
     * Test of findProvider method, of class JmriPreferencesProvider.
     * @throws java.io.IOException
     */
    public void testFindProvider() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile p = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(p.getPath(), true);
        JmriPreferencesProvider privat = JmriPreferencesProvider.findProvider(p.getPath(), false);
        assertNotNull(shared);
        assertNotNull(privat);
        assertNotSame(shared, privat);
        FileUtil.delete(p.getPath());
    }

    /**
     * Test of getPreferences method, of class JmriPreferencesProvider.
     * @throws java.io.IOException
     */
    public void testGetPreferences() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        Class<?> clazz = this.getClass();
        Preferences shared = JmriPreferencesProvider.getPreferences(project, clazz, true);
        Preferences privat = JmriPreferencesProvider.getPreferences(project, clazz, false);
        assertNotNull(shared);
        assertNotNull(privat);
        assertNotSame(shared, privat);
        try {
            assertEquals(shared.keys().length, 0);
        } catch (BackingStoreException ex) {
            assertNotNull(ex);
        }
        try {
            assertEquals(privat.keys().length, 0);
        } catch (BackingStoreException ex) {
            assertNotNull(ex);
        }
        FileUtil.delete(project.getPath());
    }

    /**
     * Test of isFirstUse method, of class JmriPreferencesProvider.
     * @throws java.io.IOException
     */
    public void testIsFirstUse() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(project.getPath(), true);
        assertEquals(shared.isFirstUse(), true);
        Preferences prefs = shared.getPreferences(this.getClass());
        prefs.put("test", "test");
        try {
            prefs.flush(); // force write
        } catch (BackingStoreException ex) {
            assertNull(ex);
        }
        shared = new JmriPreferencesProvider(project.getPath(), true);
        assertEquals(shared.isFirstUse(), false);
    }

    /**
     * Test of findCNBForClass method, of class JmriPreferencesProvider.
     */
    public void testFindCNBForClass() {
        Class<?> cls = this.getClass();
        String expResult = "jmri-util-prefs";
        String result = JmriPreferencesProvider.findCNBForClass(cls);
        assertEquals(expResult, result);
    }
    
}

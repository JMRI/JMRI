package jmri.util.prefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Randall Wood Copyright 2015, 2017
 */
public class JmriPreferencesProviderTest {

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of findProvider method, of class JmriPreferencesProvider.
     *
     * @param info the test info
     * @param folder temp dir
     * @throws java.io.IOException on unexpected test exception
     */
    @Test
    public void testFindProvider(TestInfo info, @TempDir File folder) throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile p = new Profile(info.getTestMethod().get().getName(), id, new File(folder, id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(p.getPath(), true);
        JmriPreferencesProvider privat = JmriPreferencesProvider.findProvider(p.getPath(), false);
        assertNotNull(shared);
        assertNotNull(privat);
        assertNotSame(shared, privat);
        FileUtil.delete(p.getPath());
    }

    /**
     * Test of getPreferences method, of class JmriPreferencesProvider.
     *
     * @param info the test info
     * @param folder temp dir
     * @throws java.io.IOException on unexpected test exception
     * @throws BackingStoreException on unexpected test exception
     */
    @Test
    public void testGetPreferences(TestInfo info, @TempDir File folder) throws IOException, BackingStoreException {
        // this test causes errors to be logged if the settings: portable path does not exist
        // so ensure it does
        File settings = new File(FileUtil.getPreferencesPath());
        settings.mkdirs();
        Assume.assumeTrue("settings dir exists", settings.exists());
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(info.getTestMethod().get().getName(), id, new File(folder, id));
        Class<?> clazz = this.getClass();
        Preferences shared = JmriPreferencesProvider.getPreferences(project, clazz, true);
        Preferences privat = JmriPreferencesProvider.getPreferences(project, clazz, false);
        assertNotNull(shared);
        assertNotNull(privat);
        assertNotSame(shared, privat);
        assertEquals(0, shared.keys().length);
        assertEquals(0, privat.keys().length);
        FileUtil.delete(project.getPath());
    }

    /**
     * Test of isFirstUse method, of class JmriPreferencesProvider.
     *
     * @param info the test info
     * @param folder temp dir
     * @throws java.io.IOException on unexpected test exception
     * @throws BackingStoreException on unexpected test exception
     */
    @Test
    public void testIsFirstUse(TestInfo info, @TempDir File folder) throws IOException, BackingStoreException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(info.getTestMethod().get().getName(), id, new File(folder, id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(project.getPath(), true);
        assertEquals(true, shared.isFirstUse());
        Preferences prefs = shared.getPreferences(this.getClass());
        prefs.put("test", "test");
        prefs.flush(); // force write
        shared = new JmriPreferencesProvider(project.getPath(), true);
        assertEquals(false, shared.isFirstUse());
    }

    /**
     * Test of findCNBForPackage method, of class JmriPreferencesProvider.
     */
    @Test
    public void testFindCNBForPackage() {
        // in Java 11, this would be better as:
        // ClassLoader cl = getClass().getClassLoader();
        // assertEquals("jmri-util", JmriPreferencesProvider.findCNBForPackage(cl.getDefinedPackage("jmri.util")));
        // assertEquals("jmri-jmrit-logixng", JmriPreferencesProvider.findCNBForPackage(cl.getDefinedPackage("jmri.jmrit.logixng")));
        assertEquals("jmri-util", JmriPreferencesProvider.findCNBForPackage(Package.getPackage("jmri.util")));
    }

    /**
     * Test of findCNBForClass method, of class JmriPreferencesProvider.
     */
    @Test
    public void testFindCNBForClass() {
        assertEquals("jmri-util-prefs", JmriPreferencesProvider.findCNBForClass(this.getClass()));
    }

}

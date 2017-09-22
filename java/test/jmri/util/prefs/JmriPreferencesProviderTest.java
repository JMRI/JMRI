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
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 *
 * @author Randall Wood Copyright 2015, 2017
 */
public class JmriPreferencesProviderTest {

    @Rule
    public TestName name = new TestName();
    private Path workspace;

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        this.workspace = Files.createTempDirectory(this.getClass().getSimpleName());
    }

    @After
    public void tearDown() {
        FileUtil.delete(this.workspace.toFile());
        JUnitUtil.tearDown();
    }

    /**
     * Test of findProvider method, of class JmriPreferencesProvider.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testFindProvider() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile p = new Profile(name.getMethodName(), id, new File(this.workspace.toFile(), id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(p.getPath(), true);
        JmriPreferencesProvider privat = JmriPreferencesProvider.findProvider(p.getPath(), false);
        Assert.assertNotNull(shared);
        Assert.assertNotNull(privat);
        Assert.assertNotSame(shared, privat);
        FileUtil.delete(p.getPath());
    }

    /**
     * Test of getPreferences method, of class JmriPreferencesProvider.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testGetPreferences() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(name.getMethodName(), id, new File(this.workspace.toFile(), id));
        Class<?> clazz = this.getClass();
        Preferences shared = JmriPreferencesProvider.getPreferences(project, clazz, true);
        Preferences privat = JmriPreferencesProvider.getPreferences(project, clazz, false);
        Assert.assertNotNull(shared);
        Assert.assertNotNull(privat);
        Assert.assertNotSame(shared, privat);
        try {
            Assert.assertEquals(shared.keys().length, 0);
        } catch (BackingStoreException ex) {
            Assert.assertNotNull(ex);
        }
        try {
            Assert.assertEquals(privat.keys().length, 0);
        } catch (BackingStoreException ex) {
            Assert.assertNotNull(ex);
        }
        FileUtil.delete(project.getPath());
    }

    /**
     * Test of isFirstUse method, of class JmriPreferencesProvider.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testIsFirstUse() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(name.getMethodName(), id, new File(this.workspace.toFile(), id));
        JmriPreferencesProvider shared = JmriPreferencesProvider.findProvider(project.getPath(), true);
        Assert.assertEquals(shared.isFirstUse(), true);
        Preferences prefs = shared.getPreferences(this.getClass());
        prefs.put("test", "test");
        try {
            prefs.flush(); // force write
        } catch (BackingStoreException ex) {
            Assert.assertNull(ex);
        }
        shared = new JmriPreferencesProvider(project.getPath(), true);
        Assert.assertEquals(shared.isFirstUse(), false);
    }

    /**
     * Test of findCNBForClass method, of class JmriPreferencesProvider.
     */
    @Test
    public void testFindCNBForClass() {
        Class<?> cls = this.getClass();
        String expResult = "jmri-util-prefs";
        String result = JmriPreferencesProvider.findCNBForClass(cls);
        Assert.assertEquals(expResult, result);
    }

}

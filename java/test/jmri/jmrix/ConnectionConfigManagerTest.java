package jmri.jmrix;

import apps.tests.Log4JFixture;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.profile.Profile;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for ConnectionConfigManager.
 *
 * @author Randall Wood
 */
public class ConnectionConfigManagerTest extends TestCase {

    private Path workspace;

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(ConnectionConfigManagerTest.class);
    }

    @Override
    public void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        this.workspace = Files.createTempDirectory(this.getName());
        JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
        FileUtil.delete(this.workspace.toFile());
    }

    public void testGetConnectionManufacturers() {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        String[] result = manager.getConnectionManufacturers();
        Assert.assertTrue(result.length > 1);
        Assert.assertEquals(InternalConnectionTypeList.NONE, result[0]);
        JUnitUtil.resetInstanceManager();
    }

    public void testGetConnectionTypes() {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        String[] result = manager.getConnectionTypes(InternalConnectionTypeList.NONE);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("jmri.jmrix.internal.ConnectionConfig", result[0]);
        JUnitUtil.resetInstanceManager();
    }

    /**
     * Test of initialize method, of class ConnectionConfigManager.
     *
     * @throws java.io.IOException if untested condition (most likely inability
     *                             to write to temp space) fails
     */
    public void testInitialize_EmptyProfile() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile profile = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        ConnectionConfigManager instance = new ConnectionConfigManager();
        try {
            instance.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test of getRequires method, of class ConnectionConfigManager.
     */
    public void testGetRequires() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Set<Class<? extends PreferencesManager>> result = instance.getRequires();
        Assert.assertEquals(0, result.size());
    }

    /**
     * Test of add method, of class ConnectionConfigManager.
     */
    public void testAdd() {
        ConnectionConfig c = new TestConnectionConfig();
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertTrue(instance.add(c));
        Assert.assertEquals(1, instance.getConnections().length);
        Assert.assertFalse(instance.add(c));
        Assert.assertEquals(1, instance.getConnections().length);
        NullPointerException npe = null;
        try {
            // deliberately passing invalid value
            instance.add(null);
        } catch (NullPointerException ex) {
            npe = ex;
        }
        Assert.assertNotNull(npe);
    }

    /**
     * Test of remove method, of class ConnectionConfigManager.
     */
    public void testRemove() {
        ConnectionConfig c = new TestConnectionConfig();
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertFalse(instance.remove(c));
        instance.add(c);
        Assert.assertEquals(1, instance.getConnections().length);
        Assert.assertTrue(instance.remove(c));
        Assert.assertEquals(0, instance.getConnections().length);
        Assert.assertFalse(instance.remove(c));
        Assert.assertEquals(0, instance.getConnections().length);
    }

    /**
     * Test of getConnections method, of class ConnectionConfigManager.
     */
    public void testGetConnections_0args() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        Assert.assertEquals(0, instance.getConnections().length);
        instance.add(new TestConnectionConfig());
        Assert.assertEquals(1, instance.getConnections().length);
    }

    /**
     * Test of getConnections method, of class ConnectionConfigManager.
     */
    public void testGetConnections_int() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        ConnectionConfig c = new TestConnectionConfig();
        Assert.assertTrue(instance.add(c));
        Assert.assertEquals(c, instance.getConnections(0));
        IndexOutOfBoundsException ioobe = null;
        try {
            // don't care about returned result - expecting exception to be thrown
            instance.getConnections(42);
        } catch (IndexOutOfBoundsException ex) {
            ioobe = ex;
        }
        Assert.assertNotNull(ioobe);
    }

    /**
     * Test of iterator method, of class ConnectionConfigManager.
     */
    public void testIterator() {
        ConnectionConfigManager instance = new ConnectionConfigManager();
        ConnectionConfig c = new TestConnectionConfig();
        instance.add(c);
        Iterator<ConnectionConfig> iterator = instance.iterator();
        Assert.assertNotNull(iterator);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(c, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    private static class TestConnectionConfig extends AbstractSimulatorConnectionConfig {

        @Override
        protected void setInstance() {
            // do nothing
        }

        @Override
        public String name() {
            return this.getClass().getName();
        }
    }
}

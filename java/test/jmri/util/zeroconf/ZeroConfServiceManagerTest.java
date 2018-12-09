package jmri.util.zeroconf;

import java.util.HashMap;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 * @author Randall Wood Copyright (C) 2016
 */
public class ZeroConfServiceManagerTest {

    private static final String HTTP = "_http._tcp.local.";
    private ZeroConfServiceManager manager;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // get the default manager before resetting the InstanceManager
        ZeroConfServiceManager dm = InstanceManager.containsDefault(ZeroConfServiceManager.class)
                ? InstanceManager.getDefault(ZeroConfServiceManager.class)
                : null;
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (dm != null) {
            dm.stopAll();
            JUnitUtil.waitFor(() -> {
                return (dm.allServices().isEmpty());
            }, "Stopping all ZeroConf Services");
        }
        // ensure the manager used for tests is also the default manager
        manager = InstanceManager.getDefault(ZeroConfServiceManager.class);
    }

    @After
    public void tearDown() throws Exception {
        manager.stopAll();
        JUnitUtil.waitFor(() -> {
            return (manager.allServices().isEmpty());
        }, "Stopping all ZeroConf Services");
        manager = null;
        JUnitUtil.tearDown();
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_String_int() {
        ZeroConfService result = manager.create(HTTP, 9999);
        Assert.assertNotNull(result);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_3args() {
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, 9999, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), result.getName());
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_6args() {
        String name = "my name"; // NOI18N
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = manager.create(HTTP, name, 9999, 1, 1, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(name, result.getName());
    }

    /**
     * Test of getKey method, of class ZeroConfService.
     */
    @Test
    public void testKey_String_String() {
        Assert.assertEquals("this.that", manager.key("THAT", "THIS"));
    }

    /**
     * Test of isPublished method, of class ZeroConfService.
     */
    @Test
    public void testIsPublished() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of publish method, of class ZeroConfService.
     */
    @Test
    public void testPublish() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
    }

    /**
     * Test of stop method, of class ZeroConfService.
     */
    @Test
    public void testStop() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
        manager.stop(instance);
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of stopAll method, of class ZeroConfService.
     */
    @Test
    public void testStopAll() {
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertFalse(manager.isPublished(instance));
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertTrue(manager.isPublished(instance));
        manager.stopAll();
        JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == false;
        }, "Stopping ZeroConf Service");
        Assert.assertFalse(manager.isPublished(instance));
    }

    /**
     * Test of allServices method, of class ZeroConfService.
     */
    @Test
    public void testAllServices() {
        Assert.assertEquals(0, manager.allServices().size());
        ZeroConfService instance = manager.create(HTTP, 9999);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getDefaultRailroadName(), instance.getName());
        Assert.assertEquals(0, manager.allServices().size());
        // can fail if platform does not release earlier stopped service within 15 seconds
        manager.publish(instance);
        Assume.assumeTrue("Timed out publishing ZeroConf Service", JUnitUtil.waitFor(() -> {
            return manager.isPublished(instance) == true;
        }));
        Assert.assertEquals(1, manager.allServices().size());
    }

}

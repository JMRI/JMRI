package jmri.util.zeroconf;

import apps.tests.Log4JFixture;
import java.util.HashMap;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;
import javax.jmdns.JmDNS;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.DNSOutgoing;
import javax.jmdns.impl.DNSIncoming;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;


@MockPolicy(Slf4jMockPolicy.class)
@PrepareForTest({ JmDNS.class})


/**
 * Tests for the ZeroConfService class
 *
 * @author Paul Bender Copyright (C) 2014
 * @author Randall Wood Copyright (C) 2016
 */
@RunWith(PowerMockRunner.class)
public class ZeroConfServiceTest {

    private static final String HTTP = "_http._tcp.local.";

    private static JmDNS jmdns;

    @BeforeClass
    public static void setUpClass() throws Exception {
        mockStatic(JmDNS.class);
        Mockito.when(JmDNS.create()).thenReturn(jmdns);
        Mockito.when(JmDNS.create(any(InetAddress.class))).thenReturn(jmdns);
        Mockito.when(JmDNS.create(any(InetAddress.class), anyString())).thenReturn(jmdns);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    class TestZeroConfServiceListener implements ZeroConfServiceListener {

       public boolean queuedService = false;
       public boolean publishedService = false;
       public boolean unpublishedService = false;

       public void serviceQueued(ZeroConfServiceEvent se){
          queuedService = true;
       }

       public void servicePublished(ZeroConfServiceEvent se) {
          publishedService = true;
       }

       public void serviceUnpublished(ZeroConfServiceEvent se) {
          unpublishedService = true;
       }

    }

    @Before
    public void setUp() throws Exception {
        ZeroConfService.reset();
        JUnitUtil.resetProfileManager();

        java.net.InetAddress addr = java.net.Inet4Address.getLoopbackAddress();
        JmDNSImpl jmdnsi = PowerMockito.spy(new JmDNSImpl(addr,"test"));

        PowerMockito.doNothing().when(jmdnsi).send(any(DNSOutgoing.class));
        PowerMockito.doNothing().when(jmdnsi).respondToQuery(any(DNSIncoming.class));
        //PowerMockito.doNothing().when(jmdnsi).registerService(any(ServiceInfo.class));
        //PowerMockito.doNothing().when(jmdnsi).unregisterService(any(ServiceInfo.class));
        jmdns = jmdnsi;
    }

    @After
    public void tearDown() throws Exception {
        ZeroConfService.reset();
        jmdns = null;
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_String_int() {
        ZeroConfService result = ZeroConfService.create(HTTP, 9999);
        Assert.assertNotNull(result);
        Assert.assertEquals(WebServerPreferences.getDefault().getRailroadName(), result.name());
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_3args() {
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = ZeroConfService.create(HTTP, 9999, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(WebServerPreferences.getDefault().getRailroadName(), result.name());
    }

    /**
     * Test of create method, of class ZeroConfService.
     */
    @Test
    public void testCreate_6args() {
        String name = "my name"; // NOI18N
        HashMap<String, String> properties = new HashMap<>();
        ZeroConfService result = ZeroConfService.create(HTTP, name, 9999, 1, 1, properties);
        Assert.assertNotNull(result);
        Assert.assertEquals(name, result.name());
    }

    /**
     * Test of key method, of class ZeroConfService.
     */
    @Test
    public void testKey_0args() {
        String name = "my_name";
        ZeroConfService instance = ZeroConfService.create(HTTP, name, 9999, 0, 0, new HashMap<>());
        String result = instance.key();
        Assert.assertEquals(name + "." + HTTP, result);
    }

    /**
     * Test of key method, of class ZeroConfService.
     */
    @Test
    public void testKey_0args_from_2arg_create() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        String result = instance.key();
        Assert.assertEquals(WebServerPreferences.getDefault().getRailroadName().toLowerCase() + "." + HTTP, result);
    }

    /**
     * Test of key method, of class ZeroConfService.
     */
    @Test
    public void testKey_String_String() {
        String result = ZeroConfService.key("THAT", "THIS");
        Assert.assertEquals("this.that", result);
    }

    /**
     * Test of name method, of class ZeroConfService.
     */
    @Test
    public void testName() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertEquals(WebServerPreferences.getDefault().getRailroadName(), instance.name());
    }

    /**
     * Test of type method, of class ZeroConfService.
     */
    @Test
    public void testType() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertEquals(HTTP, instance.type());
    }

    /**
     * Test of serviceInfo method, of class ZeroConfService.
     */
    @Test
    public void testServiceInfo() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        ServiceInfo result = instance.serviceInfo();
        Assert.assertNotNull(result);
    }

    /**
     * Test of isPublished method, of class ZeroConfService.
     */
    @Test
    public void testIsPublished() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        instance.publish();
        Assert.assertTrue(instance.isPublished());
    }

    /**
     * Test of the publish and stop methods, of class ZeroConfService.
     */
    @Test
    public void testPublishAndStop() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        TestZeroConfServiceListener zcl = new TestZeroConfServiceListener();
        instance.addEventListener(zcl);
        // can fail if platform does not release earlier stopped service within 15 seconds
        instance.publish();
        Assume.assumeTrue("queued event received", JUnitUtil.waitFor(() -> {
           return zcl.queuedService;
        }));
        Assume.assumeTrue("published event received", JUnitUtil.waitFor(() -> {
           return zcl.publishedService;
        }));
        Assert.assertTrue(instance.isPublished());
        instance.stop();
        Assume.assumeTrue("unpublished event received", JUnitUtil.waitFor(() -> {
           return zcl.unpublishedService;
        }));
        Assert.assertFalse(instance.isPublished());
    }

    /**
     * Test of stop method, of class ZeroConfService.
     */
    @Test
    public void testStop() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        instance.publish();
        // note that we don't make sure the service
        // is actually published before calling stop,
        // so this is different than the PublishAndStop test.
        instance.stop();
        JUnitUtil.waitFor(() -> {
            return instance.isPublished() == false;
        }, "Stopping ZeroConf Service");
        Assert.assertFalse(instance.isPublished());
    }

    /**
     * Test of stopAll method, of class ZeroConfService.
     */
    @Test
    public void testStopAll() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        TestZeroConfServiceListener zcl = new TestZeroConfServiceListener();
        instance.addEventListener(zcl);
        // can fail if platform does not release earlier stopped service within 15 seconds
        instance.publish();
        Assume.assumeTrue("queued event received", JUnitUtil.waitFor(() -> {
           return zcl.queuedService;
        }));
        Assume.assumeTrue("published event received", JUnitUtil.waitFor(() -> {
           return zcl.publishedService;
        }));
        Assert.assertTrue(instance.isPublished());
        ZeroConfService.stopAll();
        Assume.assumeTrue("unpublished event received", JUnitUtil.waitFor(() -> {
           return zcl.unpublishedService;
        }));
        Assert.assertFalse(instance.isPublished());
    }

    /**
     * Test of that the right messages are sent during the publish and stop 
     * stop sequence.  Does not check status of the JmDNS instance.
     */
    @Test
    public void testPublishAndStopMessageSequence() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertFalse(instance.isPublished());
        TestZeroConfServiceListener zcl = new TestZeroConfServiceListener();
        instance.addEventListener(zcl);
        // can fail if platform does not release earlier stopped service within 15 seconds
        instance.publish();
        Assert.assertTrue("queued event received", JUnitUtil.waitFor(() -> {
           return zcl.queuedService;
        }));
        Assert.assertTrue("published event received", JUnitUtil.waitFor(() -> {
           return zcl.publishedService;
        }));
        instance.stop();
        Assert.assertTrue("unpublished event received", JUnitUtil.waitFor(() -> {
           return zcl.unpublishedService;
        }));
    }

    /**
     * Test of allServices method, of class ZeroConfService.
     */
    @Test
    public void testAllServices() {
        Assert.assertEquals(0, ZeroConfService.allServices().size());
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        Assert.assertEquals(WebServerPreferences.getDefault().getDefaultRailroadName(), instance.name());
        Assert.assertEquals(0, ZeroConfService.allServices().size());
        instance.publish();
        Assert.assertEquals(1, ZeroConfService.allServices().size());
        Assert.assertTrue(ZeroConfService.allServices().contains(instance));
    }

}

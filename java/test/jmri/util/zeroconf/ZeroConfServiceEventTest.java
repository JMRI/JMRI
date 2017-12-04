package jmri.util.zeroconf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import jmri.util.JUnitUtil;
import javax.jmdns.JmDNS;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.DNSOutgoing;
import javax.jmdns.impl.DNSIncoming;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
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
 *
 * @author Paul Bender Copyright (C) 2017	
 */
@RunWith(PowerMockRunner.class)
public class ZeroConfServiceEventTest {

    private static final String HTTP = "_http._tcp.local.";

    private static JmDNS jmdns;

    @Test
    public void testCTor() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        JmDNS jmdns[] = ZeroConfService.netServices().values().toArray(new JmDNS[0]);
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance,jmdns[0]);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetService() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        JmDNS jmdns[] = ZeroConfService.netServices().values().toArray(new JmDNS[0]);
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance,jmdns[0]);
        Assert.assertNotNull("Service",t.getService());
    }

    @Test
    public void testGetDNS() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        JmDNS jmdns[] = ZeroConfService.netServices().values().toArray(new JmDNS[0]);
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance,jmdns[0]);
        Assert.assertEquals("DNS",jmdns[0],t.getDNS());
    }

    @Test
    @Ignore("Causing NPE on appveyor, possibly due to mocking")
    public void testGetAddress() {
        ZeroConfService instance = ZeroConfService.create(HTTP, 9999);
        JmDNS jmdns[] = ZeroConfService.netServices().values().toArray(new JmDNS[0]);
        ZeroConfServiceEvent t = new ZeroConfServiceEvent(instance,jmdns[0]);
        Assert.assertNotNull("address",t.getAddress());
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

}

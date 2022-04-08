package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PortTest {

    @Test
    public void testCTor() {
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testInterfaceMethods() {
        Assertions.assertTrue(t.status(),"port always connected");
        Assertions.assertNull(t.getInputStream(),"no input stream as loopback");
        Assertions.assertNull(t.getOutputStream(),"no output stream as loopback");
        Assertions.assertEquals("invalid request", t.openPort("portName", "appName"),"open port invalid request");
    }

    @Test
    public void testPortName() {
        Assertions.assertEquals(1, t.getPortNames().size(),"none is only option");
        Assertions.assertEquals("(none)", t.getPortNames().firstElement(),"none option");
    }

    @Test
    public void testBaudRates() {
        Assertions.assertEquals(t.validBaudNumbers().length, t.validBaudRates().length,"array lengths match");
        Assertions.assertEquals(1, t.validBaudNumbers().length,"only 1 option");
        Assertions.assertEquals(0,t.validBaudNumbers()[0],"0 baud");
    }

    @Test
    public void testConfigure() {
        CanSystemConnectionMemo memo = t.getSystemConnectionMemo();
        Assertions.assertNotNull(memo);
        
        t.configure();
        
        TrafficController tc = memo.getTrafficController();
        Assertions.assertNotNull(tc,"trafficcontroller not null and attatched to memo");
        Assertions.assertNotNull(jmri.InstanceManager.getNullableDefault(jmri.SensorManager.class),"memo configure managers called");
        
        tc.terminateThreads();
    }

    private Port t;

    @BeforeEach
    public void setUp() {
        t = new Port();
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        t.dispose(); // closes the CanSystemConnectionMemo created by the Port
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortTest.class);

}

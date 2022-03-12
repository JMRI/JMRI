package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MergConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    public void testCTor() {
        MergConnectionConfig t = new MergConnectionConfig();
        Assertions.assertNotNull(t, "exists");
        t.dispose();
    }

    @Test
    public void testMergName() {
        MergConnectionConfig t = new MergConnectionConfig();
        Assertions.assertEquals("CAN via MERG Network Interface", t.name(),"MERG name");
        t.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MergConnectionConfigTest.class);

}

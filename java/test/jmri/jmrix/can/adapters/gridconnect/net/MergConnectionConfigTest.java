package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MergConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    public void testMergName() {
        Assertions.assertEquals("CAN via MERG Network Interface", cc.name(),"MERG name");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new MergConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        if ( cc !=null ){
            cc.dispose();
        }
        cc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MergConnectionConfigTest.class);

}

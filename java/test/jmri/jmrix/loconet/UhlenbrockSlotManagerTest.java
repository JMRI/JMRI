package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UhlenbrockSlotManagerTest {
    
    @Test
    public void testCTor() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        UhlenbrockSlotManager t = new UhlenbrockSlotManager(lnis);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockSlotManagerTest.class);

}

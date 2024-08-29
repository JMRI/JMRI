package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class WarrantTableFrameTest {

    @Test
    public void testCTor() {
        WarrantTableFrame t = WarrantTableFrame.getDefault();
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantTableFrameTest.class);

}

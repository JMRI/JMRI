package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantTableModelTest {

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCTor() {

        WarrantTableFrame f = WarrantTableFrame.getDefault();
        WarrantTableModel t = new WarrantTableModel(f);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantTableModelTest.class);

}

package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusNodeTablePane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusNodeTablePaneTest {

    @Test
    public void testCtor() {
        CbusNodeTablePane t = new CbusNodeTablePane();
        Assertions.assertNotNull(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTablePaneTest.class);

}

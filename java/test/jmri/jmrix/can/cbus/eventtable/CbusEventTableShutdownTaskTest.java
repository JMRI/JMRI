package jmri.jmrix.can.cbus.eventtable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventTableShutdownTaskTest {

    @Test
    public void testCTor() {

        CbusEventTableShutdownTask t = new CbusEventTableShutdownTask("CBUS Test Shutdown Task",null);
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

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableShutdownTaskTest.class);

}

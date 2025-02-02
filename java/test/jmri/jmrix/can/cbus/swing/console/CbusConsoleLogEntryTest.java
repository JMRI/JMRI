package jmri.jmrix.can.cbus.swing.console;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusConsoleLogEntry
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusConsoleLogEntryTest  {

    @Test
    public void testCbusConsoleLogEntryCtor() {
        CbusConsoleLogEntry t = new CbusConsoleLogEntry("Frame","Decoded",-1); // -1 = No Highlight
        Assertions.assertNotNull( t, "exists");
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

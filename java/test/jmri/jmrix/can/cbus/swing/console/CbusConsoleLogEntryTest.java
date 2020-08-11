package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusConsoleLogEntry
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusConsoleLogEntryTest  {

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes sure there isn't an exception.
        CbusConsoleLogEntry t = new CbusConsoleLogEntry("Frame","Decoded",-1); // -1 = No Highlight
        Assert.assertNotNull("exists",t);
        
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

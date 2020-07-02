package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusNodeFcuTablePane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2019
 */
public class CbusNodeFcuTablePaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeFcuTablePane t = new CbusNodeFcuTablePane();
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
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFcuTablePaneTest.class);

}

package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeInfoPaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeInfoPane t = new CbusNodeInfoPane(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeInfoPane t = new CbusNodeInfoPane(null);
        
        CbusNode nd = new CbusNode(null,12345);
        nd.getNodeParamManager().setParameters(new int[]{8,165,89,10,4,5,3,4,8});
        
        t.setNode(nd);
        
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeInfoPaneTest.class);

}

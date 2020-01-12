package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        CbusNodeInfoPane t = new CbusNodeInfoPane();
        Assert.assertNotNull("exists",t);
        t = null;
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeInfoPane t = new CbusNodeInfoPane();
        
        CbusNode nd = new CbusNode(null,12345);
        nd.setParameters(new int[]{8,165,89,10,4,5,3,4,8});
        
        t.initComponents(nd);
        
        Assert.assertNotNull("exists",t);
        
        nd = null;
        t = null;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeInfoPaneTest.class);

}

package jmri.jmrix.can.cbus.node;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeStatsTest {

    @Test
    public void testCTor() {
        CbusNodeStats t = new CbusNodeStats(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testGetNodeTypeName(){
    
        CbusNode node = new CbusNode(null,123);
        Assert.assertEquals("Unset params Name", "", node.getNodeStats().getNodeTypeName());
        
        node.getNodeParamManager().setParameters(new int[]{8,-1,-1,-1,-1,-1,-1,-1,-1});
        Assert.assertEquals("Initial set params All unknown", "", node.getNodeStats().getNodeTypeName());
        
        node.getNodeParamManager().setParameters(new int[]{8,-1,-1,3,-1,-1,-1,-1,-1});
        Assert.assertEquals("Initial set params Just 3", "", node.getNodeStats().getNodeTypeName());
        
        node.getNodeParamManager().setParameters(new int[]{8,1,-1,-1,-1,-1,-1,-1,-1});
        Assert.assertEquals("Initial set params Just 1", "", node.getNodeStats().getNodeTypeName());
        
        node.getNodeParamManager().setParameters(new int[]{8,1,-1,3,-1,-1,-1,-1,-1});
        Assert.assertEquals("Initial set params Just 1 3", "", node.getNodeStats().getNodeTypeName());
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventManagerTest.class);

}

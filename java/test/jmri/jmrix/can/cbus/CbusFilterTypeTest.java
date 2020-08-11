package jmri.jmrix.can.cbus;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusFilterTypeTest {

    @Test
    public void testExists() {
        Assert.assertNotNull("exists",CbusFilterType.getCatHeads());
    }
    
    @Test
    public void testToolTip() {
        Assert.assertNull("a category without a tip",(CbusFilterType.CFIN.getToolTip()));
        Assert.assertNotNull("exists",CbusFilterType.CFEVENT.getToolTip());
        
        String cfonTip = CbusFilterType.CFON.getToolTip();
        Assert.assertNotNull("CFON ToolTip exists",cfonTip);
        Assert.assertTrue("tip found ", cfonTip.length() > 100 );
        
        // tip chosen at random, testing text retrieval mechanism, not the text
        Assert.assertEquals(
    "<html>FCLK : Fast Clock : Used to implement a fast clock for the layout.<br><html>", 
            CbusFilterType.CFCLOCK.getToolTip()
        );
    }
    
    @Test
    public void testGetCategory() {
        Assert.assertEquals("CFIN cat null",null,CbusFilterType.CFIN.getCategory());
        Assert.assertEquals("CFIN cat null",CbusFilterType.CFNODE,CbusFilterType.CFNODES.getCategory());    
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

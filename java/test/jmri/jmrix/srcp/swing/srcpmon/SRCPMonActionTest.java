package jmri.jmrix.srcp.swing.srcpmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class SRCPMonActionTest {
        
     private jmri.jmrix.srcp.SRCPSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SRCPMonAction exists",new SRCPMonAction("Test",memo) );
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.srcp.SRCPSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.srcp.SRCPSystemConnectionMemo.class,memo);

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

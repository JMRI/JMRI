package jmri.jmrix.srcp.swing.srcpmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class SRCPMonActionTest {
        
     private jmri.jmrix.srcp.SRCPSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SRCPMonAction exists",new SRCPMonAction() );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.srcp.SRCPSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.srcp.SRCPSystemConnectionMemo.class,memo);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

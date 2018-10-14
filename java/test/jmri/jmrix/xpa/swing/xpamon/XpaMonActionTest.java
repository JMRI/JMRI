package jmri.jmrix.xpa.swing.xpamon;

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
public class XpaMonActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("XpaMonAction exists",new XpaMonAction() );
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.jmrix.xpa.XpaSystemConnectionMemo memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

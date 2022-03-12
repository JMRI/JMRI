package jmri.jmrix.xpa.swing.xpamon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaMonActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("XpaMonAction exists",new XpaMonAction() );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.jmrix.xpa.XpaSystemConnectionMemo memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

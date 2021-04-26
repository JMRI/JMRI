package jmri.jmrix.xpa.swing.xpaconfig;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaConfigureActionTest {

    private jmri.jmrix.xpa.XpaSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("XpaConfigureAction exists",new XpaConfigureAction("Test",memo) );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

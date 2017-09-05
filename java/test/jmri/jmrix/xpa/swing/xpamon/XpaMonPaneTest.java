package jmri.jmrix.xpa.swing.xpamon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("XpaMonPane exists",pane );
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.jmrix.xpa.XpaSystemConnectionMemo memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);
        pane = new XpaMonPane();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

package jmri.jmrix.lenz.xntcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XnTcpAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.xntcp.XnTcpAdapter class
 *
 * @author Paul Bender
 */
public class XnTcpAdapterTest {

    @Test
    public void testCtor() {
        XnTcpAdapter a = new XnTcpAdapter();
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

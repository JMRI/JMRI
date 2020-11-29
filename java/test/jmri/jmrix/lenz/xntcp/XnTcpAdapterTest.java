package jmri.jmrix.lenz.xntcp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XnTcpAdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.xntcp.XnTcpAdapter class
 *
 * @author Paul Bender
 */
public class XnTcpAdapterTest {

    @Test
    public void testCtor() {
        XnTcpAdapter a = new XnTcpAdapter();
        Assert.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

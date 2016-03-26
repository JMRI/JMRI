package jmri.jmrix.lenz.xntcp;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>
 * Title: XnTcpXNetPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2009</p>
 *
 * @author Paul Bender
 */
public class XnTcpXNetPacketizerTest extends TestCase {

    public void testCtor() {
        XnTcpXNetPacketizer f = new XnTcpXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation());
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public XnTcpXNetPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XnTcpXNetPacketizerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

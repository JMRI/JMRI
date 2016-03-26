package jmri.jmrix.lenz.liusbserver;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>
 * Title: LIUSBServerXNetPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2009</p>
 *
 * @author Paul Bender
 * @version $Revision: 17977 $
 */
public class LIUSBServerXNetPacketizerTest extends TestCase {

    public void testCtor() {
        LIUSBServerXNetPacketizer f = new LIUSBServerXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation());
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public LIUSBServerXNetPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBServerXNetPacketizerTest.class.getName()};
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

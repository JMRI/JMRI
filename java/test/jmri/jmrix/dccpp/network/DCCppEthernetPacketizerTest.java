package jmri.jmrix.dccpp.network;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: DCCppEthernetPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2009</p>
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class DCCppEthernetPacketizerTest extends TestCase {

    public void testCtor() {
        DCCppEthernetPacketizer f = new DCCppEthernetPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation());
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public DCCppEthernetPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppEthernetPacketizerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppEthernetPacketizerTest.class.getName());

}

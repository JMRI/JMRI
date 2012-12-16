//SimpleSensorServerTest.java

package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSensorServer class
 * @author                      Paul Bender
 * @version                     $Revision$
 */
public class SimpleSensorServerTest extends TestCase {

    public void testCtor() {
	    java.io.DataOutputStream output=new java.io.DataOutputStream(
	        new java.io.OutputStream() {
	        // null output string drops characters
	        // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) throws java.io.IOException {}
	    });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSensorServer a = new SimpleSensorServer(input,output);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure

    public SimpleSensorServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleSensorServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.simpleserver.SimpleSensorServerTest.class);

        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleSensorServerTest.class.getName());

}


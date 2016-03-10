/**
 * PacketGenFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.qsi.packetgen.PacketGenFrame class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
package jmri.jmrix.qsi.packetgen;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PacketGenFrameTest extends TestCase {

    public void testFrameCreate() {
        new PacketGenFrame();
    }

    // from here down is testing infrastructure
    public PacketGenFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PacketGenFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PacketGenFrameTest.class);
        return suite;
    }

}

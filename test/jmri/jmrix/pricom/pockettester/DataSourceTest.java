// DataSourceTest.java

package jmri.jmrix.pricom.pockettester;
import jmri.NmraPacket;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the DataSource class
 * @author		Bob Jacobsen  Copyright 2005
 * @version		$Revision: 1.1 $
 */
public class DataSourceTest extends TestCase {

    public void testCreate() {
        new DataSource();
    }


    // test version handling
    public void testVersion() {
        DataSource f = new DataSource();
        String message;

        message = "nothing interesing";
        f.nextLine(message);
        Assert.assertTrue("pass misc ", !message.equals(f.version.getText()));

        message = PocketTesterTest.version;
        f.nextLine(message);
        Assert.assertTrue("show version ", message.equals(f.version.getText()));

    }


    // from here down is testing infrastructure
    public DataSourceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DataSourceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DataSourceTest.class);
        return suite;
    }

}


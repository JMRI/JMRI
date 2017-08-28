/**
 * EasyDccMonActionTest.java
 *
 * Description:	JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.easydcc.easydccmon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

public class EasyDccMonActionTest extends TestCase {

    public void testCreate() {
        EasyDccMonAction a = new EasyDccMonAction();
        Assert.assertNotNull("exists", a);
    }

    public EasyDccMonActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EasyDccMonActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccMonActionTest.class);
        return suite;
    }

}

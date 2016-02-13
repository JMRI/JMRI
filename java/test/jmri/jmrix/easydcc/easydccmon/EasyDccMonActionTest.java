/**
 * EasyDccMonActionTest.java
 *
 * Description:	JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.easydcc.easydccmon;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccMonActionTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonActionTest.class.getName());

}

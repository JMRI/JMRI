package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class CvTableModelTest extends TestCase {

    public void testStart() {
        new CvTableModel(new JLabel(), null);
    }

    // from here down is testing infrastructure
    public CvTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CvTableModelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CvTableModelTest.class);
        return suite;
    }

    // static Logger log = LoggerFactory.getLogger(CvTableModelTest.class.getName());
}

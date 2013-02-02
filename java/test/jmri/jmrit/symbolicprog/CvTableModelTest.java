// CvTableModelTest.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class CvTableModelTest extends TestCase {

    public void testStart() {
        new CvTableModel(new JLabel(),null);
    }


    // from here down is testing infrastructure

    public CvTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CvTableModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CvTableModelTest.class);
        return suite;
    }

    // static Logger log = Logger.getLogger(CvTableModelTest.class.getName());

}

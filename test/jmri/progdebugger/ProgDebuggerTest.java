// ProgDebuggerTest.java

package jmri.progdebugger;

import java.io.*;
import junit.framework.*;
import jmri.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <P>
 * Due to existing package and class names, this is both the test
 * suite for the package, but also contains some tests for the ProgDebugger class.
 *
 * @author			Bob Jacobsen, Copyright (C) 2001, 2002
 * @version         $Revision: 1.1 $
 */
public class ProgDebuggerTest extends TestCase {

        int readValue = -1;

        public void testWriteRead() throws jmri.ProgrammerException, InterruptedException {
            Programmer p = new ProgDebugger();
            ProgListener l = new ProgListener(){
                public void programmingOpReply(int value, int status) {
                    log.debug("callback value="+value+" status="+status);
                    readValue = value;
                }
            };
            p.writeCV(4, 12, l);
            Thread.sleep(100);
            p.readCV(4, l);
            Thread.sleep(300);
            Assert.assertEquals("read back", 12, readValue);
        }

	// from here down is testing infrastructure

	public ProgDebuggerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ProgDebuggerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(ProgDebuggerTest.class);
		suite.addTest(jmri.progdebugger.DebugProgrammerManagerTest.suite());
		return suite;
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgDebuggerTest.class.getName());

}

package jmri.jmrix.lenz.xntcp;

import javax.swing.JFrame;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>Title: XnTcpXNetPacketizerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2009</p>
 * @author Paul Bender 
 * @version $Revision: 1.1 $
 */
public class XnTcpXNetPacketizerTest extends TestCase {

        public void testCtor() {
          XnTcpFrame f = new XnTcpFrame();
          Assert.assertTrue(f != null);
        }

        // from here down is testing infrastructure

    public XnTcpXNetPacketizerTest(String s) {
        super(s);
    }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XnTcpXNetPacketizerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XnTcpXNetPacketizerTest.class.getName());

}

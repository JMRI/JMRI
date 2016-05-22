// MemoryContentsTest.java

package jmri.jmrit;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Test simple functioning of MemoryContents
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */

public class MemoryContentsTest extends TestCase {

    public void testReadNormalFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        
        m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFile.hex"));
        
        Assert.assertEquals("content restarts", 864, m.nextContent(500));
    }
    
	public MemoryContentsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", MemoryContentsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MemoryContentsTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	static Logger log = Logger.getLogger(MemoryContentsTest.class.getName());

}

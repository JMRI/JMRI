package jmri.jmrit.display;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.beans.PropertyChangeEvent;

/**
 * MemoryIconTest.java
 *
 * Description:
 * @author			Bob Jacobsen  Copyright 2007
 * @version			$Revision: 1.1 $
 */
public class MemoryIconTest extends TestCase {

    MemoryIcon to = null;

	public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new MemoryIcon();
        jf.getContentPane().add(to);
        
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
            }
        };
        to.setMemory("IM1");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM1").setValue("data");

        jf.pack();
        jf.setVisible(true);

	}

	// from here down is testing infrastructure

	public MemoryIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", MemoryIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MemoryIconTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}

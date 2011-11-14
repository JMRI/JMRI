// AlignmentPanelTest.java

package jmri.jmrix.rps.reversealign;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;

/**
 * JUnit tests for the rps.AlignmentPanel class.
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class AlignmentPanelTest extends TestCase {

	public void testShow() {
		jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
                        
        AlignmentPanel panel = new AlignmentPanel();
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
	}
	
    public void testXFrameCreation() {
    	JFrame f = jmri.util.JmriJFrame.getFrame("RPS Alignment");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
    }
        

	// from here down is testing infrastructure

	public AlignmentPanelTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {AlignmentPanelTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(AlignmentPanelTest.class);
            return suite;
	}

}

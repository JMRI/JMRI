package jmri.jmrit.display;

import java.awt.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PositionableLabelTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision: 1.3 $
 */
public class PositionableLabelTest extends TestCase {

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

    JLabel to = null;

	public void testShow() {
        JFrame jf = new JFrame();
        JPanel p = new JPanel();
        jf.getContentPane().add(p);
        p.setPreferredSize(new Dimension(200,200));
        p.setLayout(null);

        // test button in upper left
        JButton whereButton = new JButton("where");
        whereButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				whereButtonPushed();
			}
		});
        whereButton.setBounds(0,0,70, 40);
        p.add(whereButton);

        to = new PositionableLabel("here");
        to.setBounds(80,80,40,40);
        p.add(to);

        jf.pack();
        jf.setVisible(true);

	}

    // animate the visible frame
    public void whereButtonPushed() {
    }
	// from here down is testing infrastructure

	public PositionableLabelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PositionableLabelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PositionableLabelTest.class);
		return suite;
	}

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}

package jmri.jmrit.display;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import java.beans.PropertyChangeEvent;

/**
 * TurnoutIconTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision: 1.2 $
 */
public class TurnoutIconTest extends TestCase {

    TurnoutIcon to = null;

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

	public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon();
        jf.getContentPane().add(to);

        // test buttons
        JButton throwButton = new JButton("throw");
        throwButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				throwButtonPushed();
			}
		});
        jf.getContentPane().add(throwButton);
        JButton closeButton = new JButton("close");
        closeButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				closeButtonPushed();
			}
		});
        jf.getContentPane().add(closeButton);
        JButton unknownButton = new JButton("unknown");
        unknownButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				unknownButtonPushed();
			}
		});
        jf.getContentPane().add(unknownButton);
        JButton inconsistentButton = new JButton("inconsistent");
        inconsistentButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				inconsistentButtonPushed();
			}
		});
        jf.getContentPane().add(inconsistentButton);

        jf.pack();
        jf.show();

	}

    // animate the visible frame
    public void throwButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "CommandedState", null, new Integer(jmri.Turnout.THROWN));
        to.propertyChange(e);
    }
    public void closeButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "CommandedState", null, new Integer(jmri.Turnout.CLOSED));
        to.propertyChange(e);
    }
    public void unknownButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "CommandedState", null, new Integer(jmri.Turnout.UNKNOWN));
        to.propertyChange(e);
    }
    public void inconsistentButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "CommandedState", null, new Integer(23));
        to.propertyChange(e);
    }

	// from here down is testing infrastructure

	public TurnoutIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {TurnoutIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(TurnoutIconTest.class);
		return suite;
	}

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}

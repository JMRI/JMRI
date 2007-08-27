package jmri.jmrit.display;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.beans.PropertyChangeEvent;

/**
 * TurnoutIconTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision: 1.5 $
 */
public class TurnoutIconTest extends TestCase {

    TurnoutIcon to = null;

	public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon();
        jf.getContentPane().add(to);
        
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
                addTurnoutManager(new jmri.managers.InternalTurnoutManager());
            }
        };
        to.setTurnout("IT1");

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
        jf.setVisible(true);

	}

    // animate the visible frame
    public void throwButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, new Integer(jmri.Turnout.THROWN));
        to.propertyChange(e);
    }
    public void closeButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, new Integer(jmri.Turnout.CLOSED));
        to.propertyChange(e);
    }
    public void unknownButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, new Integer(jmri.Turnout.UNKNOWN));
        to.propertyChange(e);
    }
    public void inconsistentButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, new Integer(23));
        to.propertyChange(e);
    }

	// from here down is testing infrastructure

	public TurnoutIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", TurnoutIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(TurnoutIconTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconTest.class.getName());

}

// RpsPositionIconTest.java
package jmri.jmrit.display;

import javax.swing.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.rps.*;


/**
 * Tests for the RpsIcon class.
 *
 * @author			Bob Jacobsen Copyright 2008
 * @version			$Revision: 1.1 $
 */
public class RpsPositionIconTest extends TestCase {

    RpsPositionIcon rpsIcon;

	public void testShow() {
        JFrame jf = new JFrame("RpsPositionIcon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        rpsIcon = new RpsPositionIcon();
        jf.getContentPane().add(rpsIcon);
        
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
                addTurnoutManager(new jmri.managers.InternalTurnoutManager());
            }
        };

        // test buttons
        JButton originButton = new JButton("Set 0,0");
        originButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				measButtonPushed(0.,0.);
			}
		});
        jf.getContentPane().add(originButton);

        JButton tentenButton = new JButton("Set 10,10");
        tentenButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				measButtonPushed(10.,10.);
			}
		});
        jf.getContentPane().add(tentenButton);

        JButton fivetenButton = new JButton("Set 5,10");
        fivetenButton.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				measButtonPushed(5.,10.);
			}
		});
        jf.getContentPane().add(fivetenButton);

        JButton loco21Button = new JButton("Loco 21");
        loco21Button.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locoButtonPushed(21);
			}
		});
        jf.getContentPane().add(loco21Button);

        JButton loco33Button = new JButton("Loco 33");
        loco33Button.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locoButtonPushed(33);
			}
		});
        jf.getContentPane().add(loco33Button);

        jf.pack();
        jf.setSize(300,300);
        jf.setVisible(true);

	}

    int id = 20;
    // animate the visible frame
    public void measButtonPushed(double x, double y) {
        Reading loco = new Reading(id, null);
        Measurement m = new Measurement(loco, x, y, 0.0, 0.133, 0, "source");
        rpsIcon.notify(m);
    }
    public void locoButtonPushed(int newID) {
        id = newID;
    }

	// from here down is testing infrastructure

	public RpsPositionIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {RpsPositionIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(RpsPositionIconTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsPositionIconTest.class.getName());

}

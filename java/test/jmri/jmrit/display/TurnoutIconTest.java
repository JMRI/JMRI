package jmri.jmrit.display;

import org.apache.log4j.Logger;
import javax.swing.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.beans.PropertyChangeEvent;

/**
 * TurnoutIconTest.java
 *
 * Description:
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class TurnoutIconTest extends jmri.util.SwingTestCase {

    TurnoutIcon to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel = 
            new jmri.jmrit.display.panelEditor.PanelEditor("Test TurnoutIcon Panel");

	public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon(panel);
        jf.getContentPane().add(to);
        
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
                addTurnoutManager(new jmri.managers.InternalTurnoutManager());
            }
        };
        Assert.assertNotNull("Instance exists", i );
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

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
            "KnownState", null, Integer.valueOf(jmri.Turnout.THROWN));
        to.propertyChange(e);
    }
    public void closeButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, Integer.valueOf(jmri.Turnout.CLOSED));
        to.propertyChange(e);
    }
    public void unknownButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, Integer.valueOf(jmri.Turnout.UNKNOWN));
        to.propertyChange(e);
    }
    public void inconsistentButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
            "KnownState", null, Integer.valueOf(23));
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
    
    protected void tearDown() { 
       // now close panel window
        java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
        for (int i=0; i<listeners.length; i++) {
            panel.getTargetFrame().removeWindowListener(listeners[i]);
        }
        junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        apps.tests.Log4JFixture.tearDown(); 
    }

	// static private Logger log = Logger.getLogger(TurnoutIconTest.class.getName());

}

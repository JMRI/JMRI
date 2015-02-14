package jmri.jmrit.display;

import jmri.util.JmriJFrame;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * MemorySpinnerIconTest.java
 *
 * Description:
 * @author			Bob Jacobsen  Copyright 2009
 * @version			$Revision$
 */
public class MemorySpinnerIconTest extends jmri.util.SwingTestCase {

    MemorySpinnerIcon tos1 = null;
    MemorySpinnerIcon tos2 = null;
    MemorySpinnerIcon tos3 = null;
    MemorySpinnerIcon toi1 = null;
    MemorySpinnerIcon toi2 = null;
    MemorySpinnerIcon toi3 = null;

    jmri.jmrit.display.panelEditor.PanelEditor panel = 
            new jmri.jmrit.display.panelEditor.PanelEditor("Test MemorySpinnerIcon Panel");

	public void testShow() {
        JmriJFrame jf = new JmriJFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        tos1 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(tos1);
        tos2 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(tos2);
        toi1 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(toi1);
        toi2 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(toi2);
        
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
            }
        };
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        Assert.assertNotNull("Instance exists", i );
        tos1.setMemory("IM1");
        tos2.setMemory("IM1");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM1").setValue("4");

        toi1.setMemory("IM2");
        toi2.setMemory("IM2");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM2").setValue(Integer.valueOf(10));

        tos3 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(tos3);
        toi3 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(toi3);
        tos3.setMemory("IM1");
        toi3.setMemory("IM2");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM1").setValue(new Float(11.58F));
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM2").setValue(new Double(0.89));
        tos1.setMemory("IM1");
        Assert.assertEquals("Spinner 1", "12",  tos1.getValue());
        tos2.setMemory("IM2");
        Assert.assertEquals("Spinner 2", "12",  tos1.getValue());
        
        jf.pack();
        jf.setVisible(true);

	}

	// from here down is testing infrastructure

	public MemorySpinnerIconTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", MemorySpinnerIconTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MemorySpinnerIconTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
    }
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

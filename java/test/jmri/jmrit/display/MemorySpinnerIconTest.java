package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * MemorySpinnerIconTest.java
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class MemorySpinnerIconTest extends jmri.util.SwingTestCase {

    MemorySpinnerIcon tos1 = null;
    MemorySpinnerIcon tos2 = null;
    MemorySpinnerIcon tos3 = null;
    MemorySpinnerIcon toi1 = null;
    MemorySpinnerIcon toi2 = null;
    MemorySpinnerIcon toi3 = null;

    jmri.jmrit.display.panelEditor.PanelEditor panel = null;

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
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

        InstanceManager.getDefault().clearAll();
        JUnitUtil.initDefaultUserMessagePreferences();

        tos1.setMemory("IM1");
        tos2.setMemory("IM1");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM1").setValue("4");

        toi1.setMemory("IM2");
        toi2.setMemory("IM2");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM2").setValue(10);

        tos3 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(tos3);
        toi3 = new MemorySpinnerIcon(panel);
        jf.getContentPane().add(toi3);
        tos3.setMemory("IM1");
        toi3.setMemory("IM2");
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM1").setValue(11.58F);
        jmri.InstanceManager.memoryManagerInstance().getMemory("IM2").setValue(0.89);
        tos1.setMemory("IM1");
        Assert.assertEquals("Spinner 1", "12", tos1.getValue());
        tos2.setMemory("IM2");
        Assert.assertEquals("Spinner 2", "12", tos1.getValue());

        jf.pack();
        jf.setVisible(true);

        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }
    }

    // from here down is testing infrastructure
    public MemorySpinnerIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", MemorySpinnerIconTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MemorySpinnerIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();

        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test MemorySpinnerIcon Panel");
        }
    }

    @Override
    protected void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            ThreadingUtil.runOnGUI(() -> {
                panel.getTargetFrame().dispose();
                JUnitUtil.dispose(panel);
            });
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}

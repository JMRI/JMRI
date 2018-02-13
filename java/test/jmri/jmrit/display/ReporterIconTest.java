package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ReporterIcon.
 * <p>
 * Description:
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class ReporterIconTest extends PositionableTestBase {

    private ReporterIcon to = null;

    @Test
    public void testShowSysName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.pack();
        jf.setVisible(true);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowNumericAddress() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);


        jf.pack();
        jf.setVisible(true);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initReporterManager();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new PanelEditor("Test ReporterIcon Panel");
            p = to = new ReporterIcon(editor);

            // create objects to test
            InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1");
            to.setReporter("IR1");
            InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1").setReport("data");
            NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
            to.setIcon(icon);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}

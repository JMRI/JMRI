package jmri.jmrit.progsupport;

import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import jmri.InstanceManager;
import jmri.ProgrammerScaffold;
import jmri.managers.DefaultProgrammerManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ProgServiceModePane
 *
 * @author	Bob Jacobsen 2008
 */
public class ProgServiceModePaneTest {

    @Test
    public void testCreateHorizontalNone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create and show
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Horizontal None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 0);
        f.setVisible(true);
    }

    @Test
    public void testCreateHorizontalDIRECTBYTEMODE() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // add dummy DCC
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(
                (new ProgrammerScaffold(DefaultProgrammerManager.DIRECTBYTEMODE))));
        Assert.assertTrue("programer manager available", InstanceManager.getDefault(jmri.ProgrammerManager.class) != null);
        // create and show
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Horizontal DIRECTBYTEMODE");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 100);
        f.setVisible(true);
    }

    @Test
    public void testCreateVerticalNone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create and show
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("Vertical None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.Y_AXIS,
                        new ButtonGroup()));
        f.pack();
        f.setLocation(0, 200);
        f.setVisible(true);
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // clear InstanceManager
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}

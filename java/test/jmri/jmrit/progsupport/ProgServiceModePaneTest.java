package jmri.jmrit.progsupport;

import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import jmri.InstanceManager;
import jmri.ProgrammerScaffold;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
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
        JmriJFrame f = new JmriJFrame("Horizontal None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 0);
        f.setVisible(true);
        f.dispose();
    }

    @Test
    public void testCreateHorizontalDIRECTBYTEMODE() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // add dummy DCC
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(
                (new ProgrammerScaffold(DefaultProgrammerManager.DIRECTBYTEMODE))));
        Assert.assertNotNull("programer manager available", InstanceManager.getDefault(jmri.ProgrammerManager.class));
        // create and show
        JmriJFrame f = new JmriJFrame("Horizontal DIRECTBYTEMODE");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 100);
        f.setVisible(true);
        f.dispose();
    }

    @Test
    public void testCreateVerticalNone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create and show
        JmriJFrame f = new JmriJFrame("Vertical None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.Y_AXIS,
                        new ButtonGroup()));
        f.pack();
        f.setLocation(0, 200);
        f.setVisible(true);
        f.dispose();
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}

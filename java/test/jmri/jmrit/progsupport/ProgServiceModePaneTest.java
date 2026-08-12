 package jmri.jmrit.progsupport;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the ProgServiceModePane
 *
 * @author Bob Jacobsen 2008
 */
public class ProgServiceModePaneTest {

    @Test
    @DisabledIfHeadless
    public void testCreateHorizontalNone() {
        // create and show
        JmriJFrame f = new JmriJFrame("Horizontal None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 0);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testCreateHorizontalDIRECTBYTEMODE() {
        // add dummy DCC
        InstanceManager.store(new DefaultProgrammerManager(
                (new ProgrammerScaffold(ProgrammingMode.DIRECTBYTEMODE))), AddressedProgrammerManager.class);
        Assertions.assertNotNull(InstanceManager.getDefault(jmri.AddressedProgrammerManager.class),
            "programer manager available");
        // create and show
        JmriJFrame f = new JmriJFrame("Horizontal DIRECTBYTEMODE");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.X_AXIS));
        f.pack();
        f.setLocation(0, 100);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testCreateVerticalNone() {
        // create and show
        JmriJFrame f = new JmriJFrame("Vertical None");
        f.getContentPane().add(
                new ProgServiceModePane(BoxLayout.Y_AXIS,
                        new ButtonGroup()));
        f.pack();
        f.setLocation(0, 200);
        f.setVisible(true);
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

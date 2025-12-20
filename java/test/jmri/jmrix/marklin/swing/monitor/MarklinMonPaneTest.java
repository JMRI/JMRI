package jmri.jmrix.marklin.swing.monitor;

import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.jmrix.marklin.*;

import jmri.util.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test simple functioning of MarklinMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class MarklinMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Test
    public void testInitComponentsMemo() {

        MarklinTrafficControlScaffold tc = new MarklinTrafficControlScaffold();
        MarklinSystemConnectionMemo memo = new MarklinSystemConnectionMemo(tc);

        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initContext(memo) ));
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> pane.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });

        AbstractMonPaneScaffold ampScaff = new AbstractMonPaneScaffold(pane);
        Assertions.assertNotNull(ampScaff);

        ((MarklinMonPane)pane).message(MarklinMessage.getEnableMain());

        JUnitUtil.waitFor( () -> !pane.getFrameText().isEmpty(), "text populated");

        Assertions.assertTrue( pane.getFrameText().contains("00 00 47 11 05 00 00 00 00 01 00 00 00"));

        ampScaff.clickClearButton();
        JUnitUtil.waitFor( () -> pane.getFrameText().isEmpty(), "text cleared");

        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.FEECOMMANDSTART);
        ((MarklinMonPane)pane).reply( r );

        JUnitUtil.waitFor( () -> !pane.getFrameText().isEmpty(), "text populated by reply");
        Assertions.assertTrue( pane.getFrameText().contains("0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0"));

        JUnitUtil.dispose(ampScaff.getWindow());
        panel.dispose();

        tc.dispose();
        memo.dispose();

    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // pane for AbstractMonPaneTestBase, panel for JmriJPanelTest
        panel = pane = new MarklinMonPane();
        title=Bundle.getMessage("MarklinMonitorTitle");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
    }
}

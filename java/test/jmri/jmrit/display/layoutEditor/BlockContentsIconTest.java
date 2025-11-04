package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;

import jmri.BlockManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;
import org.slf4j.event.Level;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class BlockContentsIconTest {

    private BlockContentsIcon to = null;

    @Test
    public void testCtor() {
        assertNotNull( to, "exists");
    }

    @Test
    public void testShowRosterEntry() {
        JFrame jf = new JmriJFrame("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        assertDoesNotThrow( () -> {
            jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));
            jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1").setValue(re);
        });

        new QueueTool().waitEmpty(100);

        ThreadingUtil.runOnGUI( () -> {
            jf.pack();
            jf.setVisible(true);
        });
        new QueueTool().waitEmpty(100);
        assertFalse(JUnitAppender.unexpectedMessageSeen(Level.WARN), "No Warn Level or higher Messages");

        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowIdTag() {
        JFrame jf = new JmriJFrame("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

        jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1").setValue(tag);
        new QueueTool().waitEmpty(100);

        ThreadingUtil.runOnGUI( () -> {
            jf.pack();
            jf.setVisible(true);
        });
        new QueueTool().waitEmpty(100);
        assertFalse( JUnitAppender.unexpectedMessageSeen(Level.WARN), "No Warn Level or higher Messages");
        assertNotNull( JemmyUtil.getLabelWithText(jf.getTitle(),tag.getDisplayName()),
            "Label with correct text value");

        JUnitUtil.dispose(jf);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        to = new BlockContentsIcon("test", new LayoutEditor());
        to.setBlock(new jmri.NamedBeanHandle<>("IB1", block));

    }

    @AfterEach
    public void tearDown() {
        if(to!=null) {
            JUnitUtil.dispose(to.getEditor());
        }
        to = null;
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }
}

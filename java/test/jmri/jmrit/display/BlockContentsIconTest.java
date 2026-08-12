package jmri.jmrit.display;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;

import jmri.BlockManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.junit.annotations.ToDo;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;

import org.slf4j.event.Level;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class BlockContentsIconTest extends PositionableLabelTest {

    @Test
    public void testNamedIconCtor() {

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        BlockContentsIcon bci = new BlockContentsIcon(icon, editor);
        bci.setIcon(icon);
        assertNotNull(bci, "BlockContentsIcon Constructor");
    }

    @Test
    public void testShowRosterEntry() throws IOException, JDOMException {

        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        jmri.InstanceManager.getDefault(BlockManager.class).provide("IB1").setValue(re);
        new QueueTool().waitEmpty();

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty();
        assertFalse(JUnitAppender.unexpectedMessageSeen(Level.WARN), "No Warn Level or higher Messages");

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowIdTag() {

        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

        jmri.InstanceManager.getDefault(BlockManager.class).provide("IB1").setValue(tag);
        new QueueTool().waitEmpty();

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty();
        assertFalse(JUnitAppender.unexpectedMessageSeen(Level.WARN), "No Warn Level or higher Messages");
        assertNotNull(jmri.util.swing.JemmyUtil.getLabelWithText(jf.getTitle(), tag.getDisplayName()),
            "Label with correct text value");

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    @Override
    @ToDo("The test in the parent class fails if there is no icon set")
    public void testGetAndSetScale() {
        // the test in the parent class fails if there is no icon for the
        // blockcontents.

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        ((BlockContentsIcon) p).setIcon(icon);
        assertEquals(1.0D, p.getScale(), 0.0, "Default Scale");
        p.setScale(5.0D);
        assertEquals(5.0D, p.getScale(), 0.0, "Scale");
    }

    @Test
    @Override
    @ToDo("The test in the parent class fails if there is no icon set")
    public void testGetAndSetRotationDegrees() {

        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        ((BlockContentsIcon) p).setIcon(icon);
        p.rotate(50);
        assertEquals(50, p.getDegrees(), "Degrees");
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.initConfigureManager();

        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        BlockContentsIcon bci = new BlockContentsIcon("foo", editor);
        bci.setBlock(new jmri.NamedBeanHandle<>("IB1", block));
        // set the memory value for testClone in PositionableTestBase
        bci.setMemory("IB1");
        p = to = bci;

    }

    @AfterEach
    @Override
    public void tearDown() {
        to = null;
        super.tearDown();
    }

}

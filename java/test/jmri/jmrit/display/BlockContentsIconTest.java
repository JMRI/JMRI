package jmri.jmrit.display;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    public void testShowAllocatedValue() {
        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        java.awt.Font font = to.getFont();

        block.setAllocatedValue("Train A");
        new QueueTool().waitEmpty();
        assertEquals("<html><i>\u2192 Train A</i></html>", to.getText(),
            "the train the Block is allocated to, in italics after an arrow");

        block.setValue("Loco 47");
        new QueueTool().waitEmpty();
        assertEquals("Loco 47", to.getText(), "the train in the Block, as it is");

        block.setValue(null);
        new QueueTool().waitEmpty();
        assertEquals("<html><i>\u2192 Train A</i></html>", to.getText(),
            "the allocated train again once the Block is empty");

        block.setAllocatedValue(null);
        new QueueTool().waitEmpty();
        assertNull(to.getText(), "nothing once the Block is no longer allocated");
        assertEquals(font, to.getFont(), "the label's font, which the panel file keeps, is never changed");
    }

    @Test
    public void testAllocatedTextNames() {
        jmri.jmrit.roster.RosterEntry re = new jmri.jmrit.roster.RosterEntry();
        re.setId("Loco 47");
        assertEquals("<html><i>\u2192 Loco 47</i></html>", BlockContentsIcon.allocatedText(re),
            "a roster entry by its name");
        assertEquals("<html><i>\u2192 A &lt;b&gt; &amp; Caf\u00e9</i></html>",
            BlockContentsIcon.allocatedText("A <b> & Caf\u00e9"),
            "only what HTML needs is escaped");
        assertEquals("<html><i>\u2192 Loco 47 tag</i></html>",
            BlockContentsIcon.allocatedText(new jmri.implementation.DefaultIdTag("ID47", "Loco 47 tag")),
            "an ID tag by its display name");
        jmri.Reportable report = new jmri.Reportable() {
            @Override
            public String toReportString() {
                return "report 47";
            }
        };
        assertEquals("<html><i>\u2192 report 47</i></html>", BlockContentsIcon.allocatedText(report),
            "anything else a reporter gives by its report");
    }

    @Test
    public void testIconMapShowsAllocatedValueAsBefore() {
        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        NamedIcon trainIcon = new NamedIcon("program:resources/icons/redTransparentBox.gif", "box");
        ((BlockContentsIcon) to).addKeyAndIcon(trainIcon, "Train A");

        block.setAllocatedValue("Train A");
        new QueueTool().waitEmpty();
        assertEquals(trainIcon, to.getIcon(), "an icon map still shows the allocated train's icon");
    }

    @Test
    public void testTrainForMenu() {
        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        BlockContentsIcon bci = (BlockContentsIcon) to;

        block.setAllocatedValue("Train A");
        new QueueTool().waitEmpty();
        assertEquals("Train A", bci.trainForMenu(), "the allocated train by its name, not the marked text");

        jmri.jmrit.roster.RosterEntry entry = new jmri.jmrit.roster.RosterEntry();
        entry.setId("Loco 47");
        block.setAllocatedValue(entry);
        new QueueTool().waitEmpty();
        assertEquals(entry, bci.trainForMenu(), "an allocated roster entry as itself");

        block.setValue("Loco 12");
        new QueueTool().waitEmpty();
        assertEquals("Loco 12", bci.trainForMenu(), "a train in the Block as it is shown, as before");

        jmri.jmrit.roster.RosterEntry inBlock = new jmri.jmrit.roster.RosterEntry();
        inBlock.setId("Loco 12");
        block.setValue(inBlock);
        new QueueTool().waitEmpty();
        assertEquals(inBlock, bci.trainForMenu(), "a roster entry in the Block as itself, as before");
    }

    @Test
    public void testDispatcherMenuForAllocatedRosterEntry() {
        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        jmri.jmrit.roster.RosterEntry entry = new jmri.jmrit.roster.RosterEntry();
        entry.setId("Loco 47");
        jmri.jmrit.dispatcher.DispatcherFrame df = org.mockito.Mockito.mock(jmri.jmrit.dispatcher.DispatcherFrame.class);
        jmri.InstanceManager.store(df, jmri.jmrit.dispatcher.DispatcherFrame.class);
        to.setEditable(false);
        block.setAllocatedValue(entry);
        new QueueTool().waitEmpty();

        // no Active Train for it: the roster entry's own items
        assertEquals(java.util.List.of("Open Throttle", Bundle.getMessage("MenuNewTrain")), menuItems(),
            "a throttle or a new train for the allocated loco");
        org.mockito.Mockito.verify(df).getActiveTrainForRoster(entry);

        // its Active Train: the train's items
        jmri.jmrit.dispatcher.ActiveTrain at = org.mockito.Mockito.mock(jmri.jmrit.dispatcher.ActiveTrain.class);
        org.mockito.Mockito.when(df.getActiveTrainForRoster(entry)).thenReturn(at);
        org.mockito.Mockito.when(at.getStatus()).thenReturn(jmri.jmrit.dispatcher.ActiveTrain.RUNNING);
        assertEquals(java.util.List.of(Bundle.getMessage("MenuTerminateTrain"), Bundle.getMessage("MenuAllocateExtra")),
            menuItems(), "the train found by its roster entry");
        jmri.InstanceManager.deregister(df, jmri.jmrit.dispatcher.DispatcherFrame.class);
    }

    private java.util.List<String> menuItems() {
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        ((BlockContentsIcon) to).showPopUp(popup);
        java.util.List<String> items = new java.util.ArrayList<>();
        for (java.awt.Component c : popup.getComponents()) {
            if (c instanceof javax.swing.JMenuItem) {
                items.add(((javax.swing.JMenuItem) c).getText());
            }
        }
        return items;
    }

    @Test
    public void testDispatcherMenuOnAllocatedBlock() {
        jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        jmri.jmrit.dispatcher.DispatcherFrame df = org.mockito.Mockito.mock(jmri.jmrit.dispatcher.DispatcherFrame.class);
        jmri.jmrit.dispatcher.ActiveTrain at = org.mockito.Mockito.mock(jmri.jmrit.dispatcher.ActiveTrain.class);
        org.mockito.Mockito.when(df.getActiveTrainForName("Train A")).thenReturn(at);
        org.mockito.Mockito.when(at.getStatus()).thenReturn(jmri.jmrit.dispatcher.ActiveTrain.RUNNING);
        jmri.InstanceManager.store(df, jmri.jmrit.dispatcher.DispatcherFrame.class);
        to.setEditable(false);

        block.setAllocatedValue("Train A");
        new QueueTool().waitEmpty();
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        org.junit.jupiter.api.Assertions.assertTrue(((BlockContentsIcon) to).showPopUp(popup),
            "the train's menu, though the train hasn't reached the Block");
        java.util.List<String> items = new java.util.ArrayList<>();
        for (java.awt.Component c : popup.getComponents()) {
            if (c instanceof javax.swing.JMenuItem) {
                items.add(((javax.swing.JMenuItem) c).getText());
            }
        }
        assertEquals(java.util.List.of(Bundle.getMessage("MenuTerminateTrain"), Bundle.getMessage("MenuAllocateExtra")),
            items, "Terminate Train and Allocate Extra Sections for the allocated train");
        jmri.InstanceManager.deregister(df, jmri.jmrit.dispatcher.DispatcherFrame.class);
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

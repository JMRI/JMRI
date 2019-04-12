package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.ToDo;
import javax.swing.JFrame;
import jmri.jmrit.catalog.NamedIcon;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.Level;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class BlockContentsIconTest extends PositionableLabelTest {

    @Test
    public void testNamedIconCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        BlockContentsIcon bci = new BlockContentsIcon(icon, editor);
        bci.setIcon(icon);
	Assert.assertNotNull("BlockContentsIcon Constructor", bci);
    }

    @Test
    public void testShowRosterEntry() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012.xml"));

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(re);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowIdTag() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.IdTag tag = new jmri.implementation.DefaultIdTag("1234");

	    jmri.InstanceManager.memoryManagerInstance().provideMemory("IM1").setValue(tag);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    @Test
    @Override
    @ToDo("The test in the parent class fails if there is no icon set")
    public void testGetAndSetScale(){
	// the test in the parent clase fails if there is no icon for the
	// blockcontents.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        ((BlockContentsIcon)p).setIcon(icon);
        Assert.assertEquals("Default Scale",1.0D,p.getScale(),0.0);
        p.setScale(5.0D);
        Assert.assertEquals("Scale",5.0D,p.getScale(),0.0);
    }
    
    @Test
    @Override
    @ToDo("The test in the parent class fails if there is no icon set")
    public void testGetAndSetRotationDegrees(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NamedIcon icon = new NamedIcon("resources/icons/redTransparentBox.gif", "box"); // 13x13
        ((BlockContentsIcon)p).setIcon(icon);
        p.rotate(50);
        Assert.assertEquals("Degrees",50,p.getDegrees());
    }

    @Before
    public void setUp() {
        super.setUp();
        JUnitUtil.initConfigureManager();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("B1");
            BlockContentsIcon bci = new BlockContentsIcon("foo", editor);
            bci.setBlock(new jmri.NamedBeanHandle<>("B1", block));
            bci.setMemory("B1");
            p = to = bci;
        }
    }

    @After
    public void tearDown() {
        to = null;
        super.tearDown();
    }

}

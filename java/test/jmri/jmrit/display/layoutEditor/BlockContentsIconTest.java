package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.BlockManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.QueueTool;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class BlockContentsIconTest {

    private BlockContentsIcon to = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", to);
    }

    @Test
    public void testShowRosterEntry() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JmriJFrame();
        jf.setTitle("Expect Roster Entry");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

        jf.getContentPane().add(new javax.swing.JLabel("| Expect roster entry: "));

        jmri.jmrit.roster.RosterEntry re = jmri.jmrit.roster.RosterEntry.fromFile(new java.io.File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));

        jmri.InstanceManager.getDefault(BlockManager.class).getBlock("IB1").setValue(re);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);
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

        jmri.InstanceManager.getDefault(BlockManager.class).getBlock("IB1").setValue(tag);
        new QueueTool().waitEmpty(100);

        jf.pack();
        jf.setVisible(true);
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("No Warn Level or higher Messages",JUnitAppender.unexpectedMessageSeen(Level.WARN));
        Assert.assertNotNull("Label with correct text value",jmri.util.swing.JemmyUtil.getLabelWithText(jf.getTitle(),tag.getDisplayName()));

        jf.setVisible(false);
        JUnitUtil.dispose(jf);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
	    if(!GraphicsEnvironment.isHeadless()){
            jmri.Block block = jmri.InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
            to = new BlockContentsIcon("test", new LayoutEditor());
            to.setBlock(new jmri.NamedBeanHandle<>("IB1", block));
	    }
    }

    @After
    public void tearDown() throws Exception {
	if(to!=null) {
           JUnitUtil.dispose(to.getEditor());
	}
	to = null;
        JUnitUtil.tearDown();
    }
}

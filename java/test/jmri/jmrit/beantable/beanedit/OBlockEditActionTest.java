package jmri.jmrit.beantable.beanedit;

import jmri.InstanceManager;

import jmri.jmrit.beantable.oblock.TableFrames;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.gui.GuiLafPreferencesManager;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.QueueTool;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OBlockEditActionTest {

    @Test
    public void testCTor() {
        OBlockEditAction obea = new OBlockEditAction(new ActionEvent(this,1,null));
        Assert.assertNotNull("exists", obea);
    }

    @Test
    public void testOBlockEditorTabbed() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlockManager obm = InstanceManager.getDefault(OBlockManager.class);
        obm.createNewOBlock("OB1");
        TableFrames tf = new TableFrames();
        tf.openBlockPathPane("OB1", null);
        // TODO Failed to open?

        // Find new table window by name
        JmriJFrame doc = JmriJFrame.getFrame(Bundle.getMessage("EditBean", "OBlock" , "OB1")); //"Edit Occupancy Block OB1");
        Assert.assertNotNull("Edit OBlock OB1 window", doc);
        new QueueTool().waitEmpty();

        // Ask to close add window
        ThreadingUtil.runOnGUI(() -> {
            doc.setVisible(false);
            JUnitUtil.dispose(doc);
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // use _tabbed interface
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockEditActionTest.class);

}

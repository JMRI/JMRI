package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author Pete Cressman Copyright (C) 2019   
 */
public class LengthPanelTest {

    OBlockManager blkMgr;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob1 = blkMgr.createNewOBlock("OB1", "a");
        LengthPanel panel = new LengthPanel(ob1, "blockLength");
        Assert.assertNotNull("exists", panel);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        blkMgr = new OBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LengthPanelTest.class);
}

package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
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
        LengthPanel panel = new LengthPanel(ob1, "blockLength", "TooltipPathLength");
        Assert.assertNotNull("exists", panel);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        blkMgr = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        blkMgr.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LengthPanelTest.class);
}

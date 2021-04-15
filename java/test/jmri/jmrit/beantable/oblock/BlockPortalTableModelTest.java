package jmri.jmrit.beantable.oblock;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import jmri.util.gui.GuiLafPreferencesManager;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BlockPortalTableModelTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        TableFrames f = new TableFrames();
        BlockPortalTableModel t = new BlockPortalTableModel(new OBlockTableModel(f));
        Assertions.assertNotNull(t, "exists");
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

    // private final static Logger log = LoggerFactory.getLogger(BlockPortalTableModelTest.class);

}

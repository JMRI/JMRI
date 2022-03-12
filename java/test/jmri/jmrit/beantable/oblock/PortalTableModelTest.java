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
public class PortalTableModelTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        TableFrames tf = new TableFrames();
        PortalTableModel ptm = new PortalTableModel(tf);
        Assertions.assertNotNull(ptm, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // use original _desktop interface
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(false);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalTableModelTest.class);

}

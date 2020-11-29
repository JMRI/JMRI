package jmri.jmrit.beantable.oblock;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import jmri.util.gui.GuiLafPreferencesManager;
import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DnDJTableTest {

    @Test
    public void testDesktopCtor() {
        // use original _desktop interface (for DnD support)
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(false);

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TableFrames tf = new TableFrames();
        OBlockTableModel obtm = new OBlockTableModel(tf);
        DnDJTable ddt = new DnDJTable(obtm, new int[0]);
        Assert.assertNotNull("exists", ddt);
        JUnitUtil.dispose(tf.getDesktopFrame());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DnDJTableTest.class);

}

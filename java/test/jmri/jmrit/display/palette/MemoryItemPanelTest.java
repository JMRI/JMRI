package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MemoryItemPanelTest {

    private ItemPalette ip;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel tableModel = PickListModel.memoryPickModelInstance(); // N11N
        Editor editor = new EditorScaffold();
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("test palette", editor);
            ip.pack();
        });
        MemoryItemPanel t = new MemoryItemPanel(ip, "IM01", "", tableModel, editor);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(ip);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryItemPanelTest.class);

}

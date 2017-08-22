package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.picker.PickListModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            ip = new ItemPalette("test palette", editor);
            ip.pack();
        });
        MemoryItemPanel t = new MemoryItemPanel(ip, "IM01", "", tableModel, editor);
        Assert.assertNotNull("exists", t);
        ip.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryItemPanelTest.class.getName());

}

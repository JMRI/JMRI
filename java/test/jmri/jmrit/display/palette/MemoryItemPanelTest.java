package jmri.jmrit.display.palette;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MemoryItemPanelTest {

    private ItemPalette ip;

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.Memory> tableModel = PickListModel.memoryPickModelInstance();
        Editor editor = new ControlPanelEditor("ED");
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("test palette", editor);
            ip.pack();
        });
        MemoryItemPanel t = new MemoryItemPanel(ip, "IM01", "", tableModel);
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(ip);
        JUnitUtil.dispose(editor);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryItemPanelTest.class);

}

package jmri.jmrit.display.palette;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JScrollPane;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Pete Cressman 2020
 */
public class ItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testShowAllTabs() {

        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        assertNotNull( editor, "exists");
        ItemPalette t = ItemPalette.getDefault("ItemPalette", editor);

        int count = ItemPalette._tabPane.getComponentCount();
        assertEquals( 17, count, "tab count");
        for (int i = count-1; i>=0; i--) {
            ItemPalette._tabPane.setSelectedIndex(i);
            JScrollPane sp = (JScrollPane) ItemPalette._tabPane.getSelectedComponent();
            ItemPanel panel = (ItemPanel) sp.getViewport().getView();
            assertNotNull( panel, "ItemPanel exists");
        }
        JUnitUtil.dispose(t);
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

}

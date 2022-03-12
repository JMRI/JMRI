package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import javax.swing.JScrollPane;

import jmri.Turnout;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TableItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel<Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        DisplayFrame df = new DisplayFrame("Table Item Panel Test"); // NOI18N
        TableItemPanel<Turnout> t = new TableItemPanel<Turnout>(df,"IS01","",tableModel); // NOI18N
        Assert.assertNotNull("exists",t); // NOI18N
        JUnitUtil.dispose(df);
    }

    @Test
    public void testShowTurnoutIcons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        Assert.assertNotNull("exists", editor);
        JFrameOperator fo = new JFrameOperator(ItemPalette.getDefault("ItemPalette", editor));

        ItemPalette._tabPane.setSelectedIndex(0);
        JScrollPane sp = (JScrollPane)ItemPalette._tabPane.getComponentAt(0);
        ItemPanel panel = (ItemPanel)sp.getViewport().getView();
        Assert.assertNotNull("ItemPanel exists", panel);
        Assert.assertEquals("ItemPanel._itemType", "Turnout", panel._itemType);

        JButtonOperator bo = new JButtonOperator(fo, Bundle.getMessage("ShowIcons"));
        bo.doClick();

        bo = new JButtonOperator(fo, Bundle.getMessage("HideIcons"));
        bo.doClick();

        bo = new JButtonOperator(fo, Bundle.getMessage("ButtonEditIcons"));
        bo.doClick();

        editor.dispose();
    }

    @Test
    public void testShowIndicatorTurnoutIcons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        Assert.assertNotNull("exists", editor);
        JFrameOperator fo = new JFrameOperator(ItemPalette.getDefault("ItemPalette", editor));

        ItemPalette._tabPane.setSelectedIndex(14);
        JScrollPane sp = (JScrollPane)ItemPalette._tabPane.getComponentAt(14);
        ItemPanel panel = (ItemPanel)sp.getViewport().getView();
        Assert.assertNotNull("ItemPanel exists", panel);
        Assert.assertEquals("ItemPanel._itemType", "IndicatorTO", panel._itemType);

        JButtonOperator bo = new JButtonOperator(fo, Bundle.getMessage("ShowIcons"));
        bo.doClick();

        bo = new JButtonOperator(fo, Bundle.getMessage("HideIcons"));
        bo.doClick();

        bo = new JButtonOperator(fo, Bundle.getMessage("ButtonEditIcons"));
        bo.doClick();

        editor.dispose();
    }

    @Test
    public void testNewSensorFamily() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        Assert.assertNotNull("exists", editor);
        JFrameOperator fo = new JFrameOperator(ItemPalette.getDefault("ItemPalette", editor));

        ItemPalette._tabPane.setSelectedIndex(1);
        JScrollPane sp = (JScrollPane)ItemPalette._tabPane.getComponentAt(1);
        ItemPanel panel = (ItemPanel)sp.getViewport().getView();
        Assert.assertNotNull("ItemPanel exists", panel);
        Assert.assertEquals("ItemPanel._itemType", "Sensor", panel._itemType);

        JButtonOperator bo = new JButtonOperator(fo, Bundle.getMessage("ShowIcons"));
        bo.doClick();
/*
        //Cannot locate the JOptionPane with "createNewFamily" title
        String labelNewFamily = Bundle.getMessage("createNewFamily");
        bo = new JButtonOperator(fo, labelNewFamily);
        bo.doClick();
//        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        new Thread(() -> {
            String title = Bundle.getMessage("ItemPaletteTitle", Bundle.getMessage(ItemPanel.NAME_MAP.get(panel._itemType)));
            JFrameOperator pfo = new JFrameOperator(title);
            JDialogOperator jdo = new JDialogOperator(pfo, labelNewFamily);
            String label = Bundle.getMessage("ButtonCancel");
            JButtonOperator but = new JButtonOperator(jdo, label);
            but.doClick();
        }).start();
 */
        editor.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TableItemPanelTest.class);
}

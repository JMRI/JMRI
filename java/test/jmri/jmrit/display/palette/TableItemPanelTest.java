package jmri.jmrit.display.palette;

import javax.swing.JScrollPane;

import jmri.Turnout;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class TableItemPanelTest {

    @Test
    public void testCTor() {
        PickListModel<Turnout> tableModel = PickListModel.turnoutPickModelInstance();
        DisplayFrame df = new DisplayFrame("Table Item Panel Test"); // NOI18N
        TableItemPanel<Turnout> t = new TableItemPanel<>(df,"IS01","",tableModel);
        Assert.assertNotNull("exists",t); // NOI18N
        JUnitUtil.dispose(df);
    }

    @Test
    public void testShowTurnoutIcons() {
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        Assert.assertNotNull("exists", editor);
        JFrameOperator fo = new JFrameOperator(ItemPalette.getDefault("ItemPalette", editor));

        ItemPalette._tabPane.setSelectedIndex(0);
        JScrollPane sp = (JScrollPane)ItemPalette._tabPane.getComponentAt(0);
        ItemPanel panel = (ItemPanel)sp.getViewport().getView();
        Assert.assertNotNull("ItemPanel exists", panel);
        Assert.assertEquals("ItemPanel._itemType", "Turnout", panel._itemType);

        new JButtonOperator(fo, Bundle.getMessage("ShowIcons")).doClick();

        new JButtonOperator(fo, Bundle.getMessage("HideIcons")).doClick();

        new JButtonOperator(fo, Bundle.getMessage("ButtonEditIcons")).doClick();

        fo.requestClose();
        fo.waitClosed();

        editor.dispose();
    }

    @Test
    public void testShowIndicatorTurnoutIcons() {
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        Assert.assertNotNull("exists", editor);
        JFrameOperator fo = new JFrameOperator(ItemPalette.getDefault("ItemPalette", editor));

        ItemPalette._tabPane.setSelectedIndex(14);
        JScrollPane sp = (JScrollPane)ItemPalette._tabPane.getComponentAt(15);
        ItemPanel panel = (ItemPanel)sp.getViewport().getView();
        Assert.assertNotNull("ItemPanel exists", panel);
        Assert.assertEquals("ItemPanel._itemType", "IndicatorTO", panel._itemType);

        new JButtonOperator(fo, Bundle.getMessage("ShowIcons")).doClick();

        new JButtonOperator(fo, Bundle.getMessage("HideIcons")).doClick();

        new JButtonOperator(fo, Bundle.getMessage("ButtonEditIcons")).doClick();

        fo.requestClose();
        fo.waitClosed();

        editor.dispose();
    }

    @Test
    public void testNewSensorFamily() {
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

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("createNewFamily"));
            new JTextFieldOperator(jdo, 0).enterText("My Family Name");
            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
        });
        t.setName("Close New Family Name Dialog Thread");
        t.start();

        String labelNewFamily = Bundle.getMessage("createNewFamily");
        bo = new JButtonOperator(fo, labelNewFamily);
        bo.doClick();
        JUnitUtil.waitFor(() -> !t.isAlive(), "New Family Name Dialog Thread Complete");

        JFrameOperator myFamilyFrameOp = new JFrameOperator("Edit Icons for My Family Name");
        myFamilyFrameOp.requestClose();
        myFamilyFrameOp.waitClosed();

        fo.requestClose();
        fo.waitClosed();

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
        JUnitUtil.tearDown();
    }

}

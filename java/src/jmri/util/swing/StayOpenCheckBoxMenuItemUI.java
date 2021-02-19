package jmri.util.swing;

/**
 * UI for StayOpenCheckBoxItem or JCheckBoxMenuItem.
 * Does not close menu that the CheckBox is in when set.
 * JCheckBoxMenuItem items in Popup menus require this UI to stay open.
 * 
 * Does NOT work with Nimbus LAF, does not display checked check-boxes.
 * 
 * @author Steve Young (C) 2021
 */
public class StayOpenCheckBoxMenuItemUI extends javax.swing.plaf.basic.BasicCheckBoxMenuItemUI {

    @Override
    protected void doClick(javax.swing.MenuSelectionManager msm) {
        menuItem.doClick(0);
    }

    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent c) {
        return new StayOpenCheckBoxMenuItemUI();
    }
}

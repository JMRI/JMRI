package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.JmriJFrame;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.jmrit.beantable.Bundle;

/**
 * Create a "Tables" menu for the main menu bar.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Matthew Harris copyright (c) 2009
 * @author Bill Hood Copyright (C) 2024
 */
public class TablesMenu extends JMenu {

    public TablesMenu() {
        super();
        setText(Bundle.getMessage("MenuTables"));

        // Re-create the menu content here, following the pattern of OperationsMenu.
        // This avoids component sharing issues and NullPointerExceptions.
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTurnoutTable"), "jmri.jmrit.beantable.TurnoutTableTabAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSensorTable"), "jmri.jmrit.beantable.SensorTableTabAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLightTable"), "jmri.jmrit.beantable.LightTableTabAction"));

        JMenu signalMenu = new JMenu(Bundle.getMessage("MenuSignals"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalTable"), "jmri.jmrit.beantable.SignalHeadTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalMastTable"), "jmri.jmrit.beantable.SignalMastTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalGroupTable"), "jmri.jmrit.beantable.SignalGroupTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalMastLogicTable"), "jmri.jmrit.beantable.SignalMastLogicTableAction"));
        add(signalMenu);

        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemReporterTable"), "jmri.jmrit.beantable.ReporterTableTabAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemMemoryTable"), "jmri.jmrit.beantable.MemoryTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemStringIOTable"), "jmri.jmrit.beantable.StringIOTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemRouteTable"), "jmri.jmrit.beantable.RouteTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLRouteTable"), "jmri.jmrit.beantable.LRouteTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixTable"), "jmri.jmrit.beantable.LogixTableAction"));

        JMenu logixNG_Menu = new JMenu(Bundle.getMessage("MenuLogixNG"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGTable"), "jmri.jmrit.beantable.LogixNGTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGModuleTable"), "jmri.jmrit.beantable.LogixNGModuleTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGTableTable"), "jmri.jmrit.beantable.LogixNGTableTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGGlobalVariableTableAction"), "jmri.jmrit.beantable.LogixNGGlobalVariableTableAction"));
        add(logixNG_Menu);

        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemBlockTable"), "jmri.jmrit.beantable.BlockTableAction"));
        if (InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed()) { // turn on or off in prefs
            add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemOBlockTable"), "jmri.jmrit.beantable.OBlockTableAction"));
        } else {
            add(new jmri.jmrit.beantable.OBlockTableAction(Bundle.getMessage("MenuItemOBlockTable")));
        }
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSectionTable"), "jmri.jmrit.beantable.SectionTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTransitTable"), "jmri.jmrit.beantable.TransitTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemAudioTable"), "jmri.jmrit.beantable.AudioTableAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemIdTagTable"), "jmri.jmrit.beantable.IdTagTableTabAction"));
        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemRailComTable"), "jmri.jmrit.beantable.RailComTableAction"));
        
        add(new JSeparator());
        JMenuItem settingsItem = new JMenuItem(Bundle.getMessage("MenuSettings"));
        add(settingsItem);
        settingsItem.addActionListener((ActionEvent e) -> {
            JmriJFrame f = new JmriJFrame(Bundle.getMessage("MenuSettings"));
            f.getContentPane().setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.anchor = java.awt.GridBagConstraints.CENTER;
            c.weightx = 1.0;

            JCheckBox showTablesMenu = new JCheckBox(Bundle.getMessage("MenuItemAddTablesMenuToMainMenu"));
            UserPreferencesManager prefMgr = InstanceManager.getDefault(UserPreferencesManager.class);
            Object pref = prefMgr.getProperty("jmri.jmrit.ToolsMenu", "showTablesMenu");
            boolean showMenu = true; // Default to true
            if (pref instanceof Boolean) {
                showMenu = (Boolean) pref;
            }
            showTablesMenu.setSelected(showMenu);
            c.gridx = 0;
            c.gridy = 0;
            f.getContentPane().add(showTablesMenu, c);

            JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
            c.gridy = 1;
            f.getContentPane().add(saveButton, c);
            saveButton.addActionListener((java.awt.event.ActionEvent ev) -> {
                prefMgr.setProperty("jmri.jmrit.ToolsMenu", "showTablesMenu", showTablesMenu.isSelected());
                JOptionPane.showMessageDialog(f,
                        Bundle.getMessage("RestartRequired"),
                        Bundle.getMessage("RestartRequiredTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
            });

            f.pack();
            f.setVisible(true);
        });
    }
}

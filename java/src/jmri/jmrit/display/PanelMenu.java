package jmri.jmrit.display;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * Create the "Panels" menu for use in a menubar.
 *
 * @author Bob Jacobsen Copyright 2003, 2004, 2010
 * @author Dave Duchamp Copyright 2007
 * @author Pete Cressman Copyright 2010
 */
public class PanelMenu extends JMenu {

    private JMenu panelsSubMenu = null;
    private JMenuItem noPanelsItem = null;
    private final PropertyChangeListener listener = this::updateMenu;

    /**
     * The single PanelMenu must accessed using
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)}.
     */
    public PanelMenu() {

        super.setText(Bundle.getMessage("MenuPanels"));

        // new panel is a submenu
        //add(new jmri.jmrit.display.NewPanelAction());
        JMenu newPanel = new JMenu(Bundle.getMessage("MenuItemNew"));
        newPanel.add(new jmri.jmrit.display.panelEditor.PanelEditorAction(Bundle.getMessage("PanelEditor")));
        newPanel.add(new jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction(Bundle.getMessage("ControlPanelEditor")));
        newPanel.add(new jmri.jmrit.display.layoutEditor.LayoutEditorAction(Bundle.getMessage("LayoutEditor")));
        newPanel.add(new jmri.jmrit.display.switchboardEditor.SwitchboardEditorAction(Bundle.getMessage("SwitchboardEditor")));
        super.add(newPanel);

        super.add(new jmri.configurexml.LoadXmlUserAction(Bundle.getMessage("MenuItemLoad")));
        super.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));
        super.add(new jmri.jmrit.revhistory.swing.FileHistoryAction(Bundle.getMessage("MenuItemShowHistory")));
        super.add(new JSeparator());
        panelsSubMenu = new JMenu(Bundle.getMessage("MenuShowPanel"));
        // Add the 'No Panels' item to the sub-menu
        noPanelsItem = new JMenuItem(Bundle.getMessage("MenuItemNoPanels"));
        noPanelsItem.setEnabled(false);
        panelsSubMenu.add(noPanelsItem);
        super.add(panelsSubMenu);
        super.add(new JSeparator());
        super.add(new jmri.jmrit.jython.RunJythonScript(Bundle.getMessage("MenuItemScript")));
        super.add(new jmri.jmrit.automat.monitor.AutomatTableAction(Bundle.getMessage("MenuItemMonitor")));
        super.add(new jmri.jmrit.jython.JythonWindow(Bundle.getMessage("MenuItemScriptLog")));
        super.add(new jmri.jmrit.jython.InputWindowAction(Bundle.getMessage("MenuItemScriptInput")));
        InstanceManager.getDefault(EditorManager.class).addPropertyChangeListener(listener);
        updateMenu(null);
    }

    private void updateMenu(@CheckForNull PropertyChangeEvent evt) {
        Set<Editor> editors = InstanceManager.getDefault(EditorManager.class).getAll();
        panelsSubMenu.removeAll();
        if (editors.isEmpty()) {
            panelsSubMenu.add(noPanelsItem);
        } else {
            editors.forEach(editor -> {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(editor.getTitle());
                ActionListener action = event -> {
                    if (editor instanceof LayoutEditor) {
                        editor.setVisible(true);
                        editor.repaint();
                    } else {
                        editor.getTargetFrame().setVisible(true);
                    }
                    updateMenuItem(editor, menuItem);
                };
                menuItem.addActionListener(action);
                updateMenuItem(editor, menuItem);
                panelsSubMenu.add(menuItem);
            });
        }
    }

    private void updateMenuItem(@Nonnull Editor editor, @Nonnull JCheckBoxMenuItem menuItem) {
        if (editor instanceof LayoutEditor) {
            menuItem.setSelected(editor.isVisible());
        } else {
            menuItem.setSelected(editor.getTargetFrame().isVisible());
        }
    }

    /**
     * Utility routine for getting the number of panels in the Panels sub menu.
     *
     * @return the number of panels
     * @deprecated since 4.19.6; use {@link java.util.Collection#size()} on the
     * results of {@link EditorManager#getAll()} instead
     */
    @Deprecated
    public int getNumberOfPanels() {
        return InstanceManager.getDefault(EditorManager.class).getAll().size();
    }

    /**
     * Delete a panel from Show Panel sub menu.
     *
     * @param panel the panel to remove from the menu
     * @deprecated since 4.19.6 without public replacement
     */
    @Deprecated
    public void deletePanel(Editor panel) {
        updateMenu(null);
    }

    /**
     * Add an Editor panel to Show Panels sub menu.
     *
     * @param panel the panel to add to the menu
     * @deprecated since 4.19.6 without public replacement
     */
    @Deprecated
    public void addEditorPanel(final Editor panel) {
        updateMenu(null);
    }

    /**
     * Update an Editor type panel in Show Panels sub menu.
     *
     * @param panel the panel to update
     * @deprecated since 4.19.6 without public replacement
     */
    @Deprecated
    public void updateEditorPanel(Editor panel) {
        updateMenu(null);
    }

    /**
     * Rename an Editor type panel in Show Panels submenu.
     *
     * @param panel the panel to rename
     * @deprecated since 4.19.6 without public replacement
     */
    @Deprecated
    public void renameEditorPanel(Editor panel) {
        updateMenu(null);
    }

    /**
     * Determine if named panel already exists.
     *
     * @param name the name to test
     * @return true if name is in use; false otherwise
     * @deprecated since 4.19.6; use
     * {@link EditorManager#contains(java.lang.String)} instead
     */
    @Deprecated
    public boolean isPanelNameUsed(String name) {
        return InstanceManager.getDefault(EditorManager.class).contains(name);
    }

    /**
     * @param name the name of the editor
     * @return the editor or null if there is no matching editor
     * @deprecated since 4.19.6; use {@link EditorManager#get(java.lang.String)}
     * instead
     */
    @Deprecated
    public Editor getEditorByName(String name) {
        return InstanceManager.getDefault(EditorManager.class).get(name);
    }

    /**
     *
     * @return the list of Editors
     * @deprecated since 4.19.6; use {@link EditorManager#getAll()} instead
     */
    @Deprecated
    public ArrayList<Editor> getEditorPanelList() {
        return new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll());
    }

    /**
     *
     * @return the list of LayoutEditors
     * @deprecated since 4.19.6; use
     * {@link EditorManager#getAll(java.lang.Class)} instead
     */
    @Deprecated
    public ArrayList<LayoutEditor> getLayoutEditorPanelList() {
        return new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class));
    }
}

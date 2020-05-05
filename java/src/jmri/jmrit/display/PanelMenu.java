package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    private void updateMenu(PropertyChangeEvent evt) {
        // an EditorManager notifies if an editor is added, removed, or renamed
        // when editors are added or removed from the EditorManager, the event
        // source is the EditorManager and the event is indexed
        // when an editor is renamed, the event source is the Editor, and event
        // is not indexed
        if (evt.getSource().equals(InstanceManager.getDefault(EditorManager.class))
                && evt instanceof IndexedPropertyChangeEvent) {
            if (evt.getNewValue() != null) {
                addEditorPanel((Editor) evt.getNewValue());
            } else if (evt.getOldValue() != null) {
                deletePanel((Editor) evt.getOldValue());
            }
        } else if (evt.getSource() instanceof Editor) {
            Editor e = (Editor) evt.getSource();
            if ("title".equals(evt.getPropertyName())) {
                renameEditorPanel(e);
            } else {
                updateEditorPanel(e);
            }
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
     */
    public void deletePanel(Editor panel) {
        log.debug("deletePanel");
        ArrayList<Editor> panelsList = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll());
        if (panelsList.isEmpty()) {
            return;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Object o = panelsList.get(i);
            if (o == panel) {
                // Editors that are their own TargetFrame dispose themselves
                if (!panel.equals(panel.getTargetFrame())) {
                    panel.getTargetFrame().dispose();
                }
                panelsList.remove(panel);
                panelsSubMenu.remove(i);
                // If there are no panels on the list,
                // replace the 'No Panels' menu item
                if (panelsList.isEmpty()) {
                    panelsSubMenu.add(noPanelsItem);
                }
                return;
            }
        }
    }

    /**
     * Add an Editor panel to Show Panels sub menu.
     *
     * @param panel the panel to add to the menu
     */
    public void addEditorPanel(final Editor panel) {
        // If this is the first panel, remove the 'No Panels' menu item
        ArrayList<Editor> panelsList = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll());
        if (panelsList.isEmpty()) {
            panelsSubMenu.remove(noPanelsItem);
        }
        panelsList.add(panel);
        ActionListener a = (ActionEvent e) -> {
            if (panel instanceof LayoutEditor) {
                panel.setVisible(true);
                panel.repaint();
            } else {
                panel.getTargetFrame().setVisible(true);
            }
            updateEditorPanel(panel);
        };
        JCheckBoxMenuItem r = new JCheckBoxMenuItem(panel.getTitle());
        r.addActionListener(a);
        panelsSubMenu.add(r);
        updateEditorPanel(panel);
    }

    /**
     * Update an Editor type panel in Show Panels sub menu.
     *
     * @param panel the panel to update
     */
    public void updateEditorPanel(Editor panel) {
        ArrayList<Editor> panelsList = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll());
        if (panelsList.isEmpty()) {
            return;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Object o = panelsList.get(i);
            if (o == panel) {
                JMenuItem subMenu = panelsSubMenu.getItem(i);
                if (subMenu instanceof JCheckBoxMenuItem) {
                    JCheckBoxMenuItem r = (JCheckBoxMenuItem) subMenu;
                    if (panel instanceof LayoutEditor) {
                        if (panel.isVisible()) {
                            r.setSelected(true);
                        } else {
                            r.setSelected(false);
                        }
                    } else {
                        if (panel.getTargetFrame().isVisible()) {
                            r.setSelected(true);
                        } else {
                            r.setSelected(false);
                        }
                    }
                }
                return;
            }
        }
    }

    /**
     * Rename an Editor type panel in Show Panels submenu.
     *
     * @param panel the panel to rename
     */
    public void renameEditorPanel(Editor panel) {
        ArrayList<Editor> panelsList = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll());
        if (panelsList.isEmpty()) {
            return;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Object o = panelsList.get(i);
            if (o == panel) {
                JMenuItem subMenu = panelsSubMenu.getItem(i);
                if (subMenu instanceof JCheckBoxMenuItem) {
                    JCheckBoxMenuItem r = (JCheckBoxMenuItem) subMenu;
                    r.setText(panel.getTitle());
                }
                return;
            }
        }
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
     * @deprecated since 4.19.6; use {@link EditorManager#get(java.lang.String)} instead
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
     * @deprecated since 4.19.6; use {@link EditorManager#getAll(java.lang.Class)} instead
     */
    @Deprecated
    public ArrayList<LayoutEditor> getLayoutEditorPanelList() {
        return new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class));
    }

    private final static Logger log = LoggerFactory.getLogger(PanelMenu.class);

}

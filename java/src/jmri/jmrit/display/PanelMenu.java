package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create the default "Panels" menu for use in a menubar.
 * <p>
 * Also manages the Show Panel menu for all Editor panels.
 *
 * @author Bob Jacobsen Copyright 2003, 2004, 2010
 * @author Dave Duchamp Copyright 2007
 * @author Pete Cressman Copyright 2010
 */
public class PanelMenu extends JMenu {

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
    }

    // operational variables
    private JMenu panelsSubMenu = null;
    private JMenuItem noPanelsItem = null;
    private final ArrayList<Editor> panelsList = new ArrayList<>();

    /**
     * Provide method to reference this panel menu
     *
     * @return get the single instance of this menu
     * @deprecated since 4.9.3; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    static public PanelMenu instance() {
        return InstanceManager.getDefault(PanelMenu.class);
    }

    /**
     * Has no effect; retained for backwards compatibility.
     *
     * @deprecated since 4.9.3; use
     * {@link jmri.InstanceManager#reset(java.lang.Class)} instead
     */
    @Deprecated
    static public void dispose() {
        // do nothing
    }

    /**
     * Utility routine for getting the number of panels in the Panels sub menu
     *
     * @return the number of panels
     */
    public int getNumberOfPanels() {
        return panelsList.size();
    }

    /**
     * Delete a panel from Show Panel sub menu
     *
     * @param panel the panel to remove from the menu
     */
    public void deletePanel(Editor panel) {
        log.debug("deletePanel");
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
     * Add an Editor panel to Show Panels sub menu
     *
     * @param panel the panel to add to the menu
     */
    public void addEditorPanel(final Editor panel) {
        // If this is the first panel, remove the 'No Panels' menu item
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
     * Update an Editor type panel in Show Panels sub menu
     *
     * @param panel the panel to update
     */
    public void updateEditorPanel(Editor panel) {
        if (panelsList.isEmpty()) {
            return;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Object o = panelsList.get(i);
            if (o == panel) {
                JCheckBoxMenuItem r = (JCheckBoxMenuItem) panelsSubMenu.getItem(i);
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
                return;
            }
        }
    }

    /**
     * Rename an Editor type panel in Show Panels sub menu
     *
     * @param panel the panel to rename
     */
    public void renameEditorPanel(Editor panel) {
        if (panelsList.isEmpty()) {
            return;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Object o = panelsList.get(i);
            if (o == panel) {
                JCheckBoxMenuItem r = (JCheckBoxMenuItem) panelsSubMenu.getItem(i);
                r.setText(panel.getTitle());
                return;
            }
        }
    }

    /**
     * Determine if named panel already exists returns true if named panel
     * already loaded
     *
     * @param name the name to test
     * @return true if name is in use; false otherwise
     */
    public boolean isPanelNameUsed(String name) {
        if (panelsList.isEmpty()) {
            return false;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            Editor editor = panelsList.get(i);
            if (editor.getTargetFrame().getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Editor getEditorByName(String name) {
        if (panelsList.isEmpty()) {
            return null;
        }
        for (int i = 0; (i < panelsList.size()); i++) {
            Editor editor = panelsList.get(i);
            if (editor.getTargetFrame().getTitle().equals(name)) {
                return editor;
            }
        }
        return null;
    }

    public ArrayList<Editor> getEditorPanelList() {
        return panelsList;
    }

    public ArrayList<LayoutEditor> getLayoutEditorPanelList() {
        ArrayList<LayoutEditor> lePanelsList = new ArrayList<>();
        panelsList.stream().filter((e) -> (e instanceof LayoutEditor)).forEachOrdered((e) -> {
            lePanelsList.add((LayoutEditor) e);
        });
        return lePanelsList;
    }

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(PanelMenu.class)) {
                return new PanelMenu();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(PanelMenu.class);
            return set;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PanelMenu.class);
}

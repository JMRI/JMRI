package jmri.jmrit.display;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
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
        StreamSupport.stream(ServiceLoader.load(EditorActionFactory.class).spliterator(), false)
                .sorted(Comparator.comparing(EditorActionFactory::getTitle))
                .forEach(factory -> newPanel.add(factory.createAction()));
        super.add(newPanel);

        panelsSubMenu = new JMenu(Bundle.getMessage("MenuShowPanel"));
        // Add the 'No Panels' item to the sub-menu
        noPanelsItem = new JMenuItem(Bundle.getMessage("MenuItemNoPanels"));
        noPanelsItem.setEnabled(false);
        panelsSubMenu.add(noPanelsItem);
        super.add(panelsSubMenu);

        super.add(new jmri.jmrit.display.PanelDeleteAction(Bundle.getMessage("MenuItemDeletePanel")));  // NOI18N

        InstanceManager.getDefault(EditorManager.class).addPropertyChangeListener(listener);
        updateMenu(null);
    }

    private void updateMenu(@CheckForNull PropertyChangeEvent evt) {
        Set<Editor> editors = InstanceManager.getDefault(EditorManager.class).getAll();
        panelsSubMenu.removeAll();
        if (editors.isEmpty()) {
            panelsSubMenu.add(noPanelsItem);
            this.getItem(2).setEnabled(false);  // Disable Delete Panel...
        } else {
            this.getItem(2).setEnabled(true);   // Enable Delete Panel...
            editors.forEach(editor -> {
                JMenuItem menuItem = new JMenuItem(editor.getTitle());
                ActionListener action = event -> {
                    editor.getTargetFrame().setVisible(true);
                    editor.getTargetFrame().repaint();
                };
                menuItem.addActionListener(action);
                panelsSubMenu.add(menuItem);
            });
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

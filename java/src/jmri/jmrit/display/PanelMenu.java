package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create the default "Panels" menu for use in a menubar.
 *
 * Also manages the Show Panel menu for all Editor panels.
 *
 * @author	Bob Jacobsen Copyright 2003, 2004, 2010
 * @author Dave Duchamp Copyright 2007
 * @author Pete Cressman Copyright 2010
 */
public class PanelMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -2195917129303948988L;

    /**
     * The single PanelMenu must now be accessed via the instance() method
     */
    private PanelMenu() {

        this.setText(Bundle.getMessage("MenuPanels"));

        // new panel is a submenu
        //add(new jmri.jmrit.display.NewPanelAction());
        JMenu newPanel = new JMenu(Bundle.getMessage("MenuItemNew"));
        newPanel.add(new jmri.jmrit.display.panelEditor.PanelEditorAction(Bundle.getMessage("PanelEditor")));
        newPanel.add(new jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction(Bundle.getMessage("ControlPanelEditor")));
        newPanel.add(new jmri.jmrit.display.layoutEditor.LayoutEditorAction(Bundle.getMessage("LayoutEditor")));
        add(newPanel);

        add(new jmri.configurexml.LoadXmlUserAction(Bundle.getMessage("MenuItemLoad")));
        add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));
        add(new jmri.jmrit.revhistory.swing.FileHistoryAction(Bundle.getMessage("MenuItemShowHistory")));
        add(new JSeparator());
        panelsSubMenu = new JMenu(Bundle.getMessage("MenuShowPanel"));
        // Add the 'No Panels' item to the sub-menu
        noPanelsItem = new JMenuItem(Bundle.getMessage("MenuItemNoPanels"));
        noPanelsItem.setEnabled(false);
        panelsSubMenu.add(noPanelsItem);
        add(panelsSubMenu);
        add(new JSeparator());
        add(new jmri.jmrit.jython.RunJythonScript(Bundle.getMessage("MenuItemScript")));
        add(new jmri.jmrit.automat.monitor.AutomatTableAction(Bundle.getMessage("MenuItemMonitor")));
        add(new jmri.jmrit.jython.JythonWindow(Bundle.getMessage("MenuItemScriptLog")));
        add(new jmri.jmrit.jython.InputWindowAction(Bundle.getMessage("MenuItemScriptInput")));
    }

    // operational variables
    private JMenu panelsSubMenu = null;
    private JMenuItem noPanelsItem = null;
    static private PanelMenu thisMenu = null;
    private ArrayList<Editor> panelsList = new ArrayList<Editor>();

    /**
     * Provide method to reference this panel menu
     */
    static public PanelMenu instance() {
        if (thisMenu == null) {
            thisMenu = new PanelMenu();
        }
        return thisMenu;
    }

    /**
     * Provide method to delete the refence to this menu
     */
    static public void dispose() {
        thisMenu = null;
    }

    /**
     * Utility routine for getting the number of panels in the Panels sub menu
     */
    public int getNumberOfPanels() {
        return panelsList.size();
    }

    /**
     * Delete a panel from Show Panel sub menu
     */
    public void deletePanel(Editor panel) {
        if (log.isDebugEnabled()) {
            log.debug("deletePanel");
        }
        if (panelsList.size() == 0) {
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
                if (panelsList.size() == 0) {
                    panelsSubMenu.add(noPanelsItem);
                }
                return;
            }
        }
    }

    /**
     * Add an Editor panel to Show Panels sub menu
     */
    public void addEditorPanel(final Editor panel) {
        // If this is the first panel, remove the 'No Panels' menu item
        if (panelsList.size() == 0) {
            panelsSubMenu.remove(noPanelsItem);
        }
        panelsList.add(panel);
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (panel instanceof LayoutEditor) {
                    panel.setVisible(true);
                    panel.repaint();
                } else {
                    panel.getTargetFrame().setVisible(true);
                }
                updateEditorPanel(panel);
            }
        };
        JCheckBoxMenuItem r = new JCheckBoxMenuItem(panel.getTitle());
        r.addActionListener(a);
        panelsSubMenu.add(r);
        updateEditorPanel(panel);
    }

    /**
     * Update an Editor type panel in Show Panels sub menu
     */
    public void updateEditorPanel(Editor panel) {
        if (panelsList.size() == 0) {
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
     */
    public void renameEditorPanel(Editor panel) {
        if (panelsList.size() == 0) {
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
     */
    public boolean isPanelNameUsed(String name) {
        if (panelsList.size() == 0) {
            return false;
        }
        for (int i = 0; i < panelsList.size(); i++) {
            try {
                Editor editor = panelsList.get(i);
                if (editor.getTargetFrame().getTitle().equals(name)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public Editor getEditorByName(String name) {
        if (panelsList.size() == 0) {
            return null;
        }
        for (int i = 0; (i < panelsList.size()); i++) {
            try {
                Editor editor = panelsList.get(i);
                if (editor.getTargetFrame().getTitle().equals(name)) {
                    return editor;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public ArrayList<Editor> getEditorPanelList() {
        return panelsList;
    }

    public ArrayList<LayoutEditor> getLayoutEditorPanelList() {
        ArrayList<LayoutEditor> lePanelsList = new ArrayList<LayoutEditor>();
        for (int i = 0; (i < panelsList.size()); i++) {
            try {
                LayoutEditor le = (LayoutEditor) panelsList.get(i);
                lePanelsList.add(le);
            } catch (Exception e) {
            }
        }
        return lePanelsList;
    }
    private final static Logger log = LoggerFactory.getLogger(PanelMenu.class.getName());
}

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

}

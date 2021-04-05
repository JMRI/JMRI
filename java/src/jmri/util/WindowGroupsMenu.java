package jmri.util;

import jmri.swing.WindowGroupManager;
import jmri.swing.WindowGroup;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;

import jmri.*;
import jmri.util.swing.WindowInterface;

/**
 * Creates a menu showing all open windows and allows to bring one in front
 *
 * @author Giorgio Terdina Copyright 2008
 */
public class WindowGroupsMenu extends JMenu implements javax.swing.event.MenuListener {

    public WindowGroupsMenu(WindowInterface wi) {
        super(Bundle.getMessage("MenuWindowGroups"));
        addMenuListener(this);
    }

    @Override
    public void menuSelected(MenuEvent e) {
        WindowGroup selectedWindowGroup =
                InstanceManager.getDefault(WindowGroupManager.class).getSelected();
        
        removeAll();
        
        add(new AbstractAction(Bundle.getMessage("MenuWindowGroups_Create")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = "";
                while (true) {
                    name = JOptionPane.showInputDialog(Bundle.getMessage("MenuWindowGroups_CreateWindowGroup_EnterName"), name);
                    if (name == null) break;
                    try {
                        InstanceManager.getDefault(WindowGroupManager.class).create(name);
                        break;
                    } catch (WindowGroupManager.DuplicateWindowGroupException ignore) {
                        int n = JOptionPane.showOptionDialog(null,
                                Bundle.getMessage("MenuWindowGroups_CreateWindowGroup_DuplicateName"),
                                Bundle.getMessage("MenuWindowGroups_CreateWindowGroup_TitleError"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.ERROR_MESSAGE,
                                null,
                                null,
                                null);
                        if (n == JOptionPane.CANCEL_OPTION) break;
                    }
                }
            }
        });
        
        AbstractAction abstractAction = new AbstractAction(Bundle.getMessage("MenuWindowGroups_Remove")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = JOptionPane.showConfirmDialog(
                        null,
                        Bundle.getMessage("MenuWindowGroups_RemoveWindowGroup_ConfirmRemoval",
                                selectedWindowGroup.getName()),
                        Bundle.getMessage("MenuWindowGroups_RemoveWindowGroup_TitleRemove"),
                        JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    InstanceManager.getDefault(WindowGroupManager.class).removeSelected();
                }
            }
        };
        abstractAction.setEnabled(selectedWindowGroup != null);
        add(abstractAction);
        
        add(new AbstractAction(Bundle.getMessage("MenuWindowGroups_Store")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(WindowGroupManager.class).store();
            }
        });
        
        add(new JSeparator());
        
        for (WindowGroup wg : InstanceManager.getDefault(WindowGroupManager.class).getAll()) {
            JCheckBoxMenuItem newItem = new JCheckBoxMenuItem(new AbstractAction(wg.getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JMenuItem selectedItem = (JMenuItem) e.getSource();
                    InstanceManager.getDefault(WindowGroupManager.class).select(selectedItem.getText());
                }
            });
            if (wg == selectedWindowGroup) {
                newItem.setState(true);
            }
            add(newItem);
        }
        
/*
        add(new AbstractAction(Bundle.getMessage("MenuItemMinimize")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // the next line works on Java 2, but not 1.1.8
                if (parentFrame != null) {
                    parentFrame.setState(Frame.ICONIFIED);
                }
            }
        });
        add(new JSeparator());

        int framesNumber = framesList.size();
        for (int i = 0; i < framesNumber; i++) {
            JmriJFrame iFrame = framesList.get(i);
            windowName = iFrame.getTitle();
            if (windowName.equals("")) {
                windowName = "Untitled";
            }
            JCheckBoxMenuItem newItem = new JCheckBoxMenuItem(new AbstractAction(windowName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JMenuItem selectedItem = (JMenuItem) e.getSource();
                    // Since different windows can have the same name, look for the position of the selected menu item
                    int itemCount = getItemCount();
                    // Skip possible other items at the top of the menu (for example, "Minimize")
                    int firstItem = itemCount - framesList.size();
                    for (int i = firstItem; i < itemCount; i++) {
                        if (selectedItem == getItem(i)) {
                            i -= firstItem;
                            // Retrieve the corresponding window
                            if (i < framesList.size()) { // "i" should always be < framesList.size(), but it's better to make sure
                                framesList.get(i).setVisible(true);
                                framesList.get(i).setExtendedState(Frame.NORMAL);
                                return;
                            }
                        }
                    }
                }
            });
            if (iFrame == parentFrame) {
                newItem.setState(true);
            }
            add(newItem);
        }
*/
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

}

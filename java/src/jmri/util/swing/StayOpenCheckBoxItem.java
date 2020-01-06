package jmri.util.swing;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Default swing behaviour is to close a JCheckBoxMenuItem when clicked.
 * This leaves the menu open following a selection.
 * Used when long lists are displayed.
 * @author Steve Young (C) 2019
 */
public class StayOpenCheckBoxItem extends JCheckBoxMenuItem {
    
        private MenuElement[] path;
        {
            getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (getModel().isArmed() && isShowing()) {
                        path = MenuSelectionManager.defaultManager().getSelectedPath();
                    }
                }
            });
        }
    
        public StayOpenCheckBoxItem(String text) {
            super(text);
        }
    
        @Override
        public void doClick(int pressTime) {
            super.doClick(pressTime);
            MenuSelectionManager.defaultManager().setSelectedPath(path);
        }
    }
    
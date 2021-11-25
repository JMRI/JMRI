package jmri.util.swing;

import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default swing behaviour is to close a JCheckBoxMenuItem when clicked. This
 * leaves the menu open following a selection. Used when long lists are
 * displayed.
 * Does not stay open with Nimbus LAF Popup Menus,
 * does stay open with other Nimbus LAF Menus.
 * 
 * @author Steve Young (C) 2019
 */
public class StayOpenCheckBoxItem extends JCheckBoxMenuItem {

    private MenuElement[] path;

    public StayOpenCheckBoxItem(String text) {
        super(text);
        initUI();
    }
    
    public StayOpenCheckBoxItem(String text, boolean isSelected) {
        super(text, isSelected);
        initUI();
    }
    
    private void initUI(){
        putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", Boolean.TRUE);
        if (useWithLAF()){
            setUI(new StayOpenCheckBoxMenuItemUI());
            getModel().addChangeListener((ChangeEvent e) -> {
                if (getModel().isArmed() && isShowing()) {
                    path = MenuSelectionManager.defaultManager().getSelectedPath();
                }
            });
        }
    }
    
    private static final Set<String> LAFS = new HashSet<String>(Arrays.asList(
        new String[] {"Nimbus","Mac OS X"}));
    
    private static boolean useWithLAF() {
        String laf = UIManager.getLookAndFeel().getName();
        log.debug("Using LAF '{}'",laf);
        return !LAFS.contains(laf);
    }

    @Override
    public void doClick(int pressTime) {
        super.doClick(pressTime);
        if (useWithLAF()){
            MenuSelectionManager.defaultManager().setSelectedPath(path);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(StayOpenCheckBoxItem.class);
}

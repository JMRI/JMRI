package jmri.jmrit.display.palette;

import java.util.HashMap;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used when FamilyItemPanel classes add or modify icon sets
 * families.
 * @author Pete Cressman Copyright (c) 2018
 */
public class SignalHeadIconDialog extends IconDialog {
    
    public SignalHeadIconDialog(String type, String family, FamilyItemPanel parent, HashMap<String, NamedIcon> iconMap) {
        super(type, family, parent, iconMap);
    }

    @Override
    protected void makeDoneButtonPanel(JPanel buttonPanel, HashMap<String, NamedIcon> iconMap) {
        if (iconMap != null) {
            HashMap<String, NamedIcon> map = ((SignalHeadItemPanel)_parent).getFilteredIconMap(iconMap);
            _iconMap = IconDialog.clone(map);
            makeDoneButtonPanel(buttonPanel, "ButtonDone");
        } else {
            _iconMap = ItemPanel.makeNewIconMap(_type);
            makeDoneButtonPanel(buttonPanel, "addNewFamily");
        }        
        log.debug("_iconMap size = {}", _iconMap.size());
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIconDialog.class);

}


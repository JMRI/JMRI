package jmri.jmrit.display.palette;

import java.util.HashMap;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used when FamilyItemPanel classes add or modify icon sets
 * families.
 * @author Pete Cressman Copyright (c) 2018
 */
public class SignalHeadIconDialog extends IconDialog {
    
    public SignalHeadIconDialog(String type, String family, FamilyItemPanel parent) {
        super(type, family, parent);
    }

    @Override
    protected void setMap(HashMap<String, NamedIcon> iconMap) {
        if (iconMap != null) {
            iconMap = ((SignalHeadItemPanel)_parent).getFilteredIconMap(iconMap);
        }
        super.setMap(iconMap);
        log.debug("_iconMap size = {}", _iconMap.size());
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIconDialog.class);

}


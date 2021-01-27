package jmri.jmrit.display.palette;

import java.util.HashMap;
import javax.annotation.Nonnull;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;

/**
 * ItemPanel for PortalIcons.
 * 
* @author Pete Cressman Copyright (c) 2013, 2020
 */
public class PortalItemPanel extends FamilyItemPanel {

    public PortalItemPanel(DisplayFrame parentFrame, String type, String family) {
        super(parentFrame, type, family);
    }

    @Override
    protected void makeFamiliesPanel() {
        _suppressDragging = true;
        super.makeFamiliesPanel();
        _previewPanel.setVisible(false);
    }

    @Override
    protected String getDisplayKey() {
        return "toArrow";
    }

    @Override
    @Nonnull
    protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> map = super.makeNewIconMap(type);
        map.put(PortalIcon.HIDDEN, new NamedIcon("resources/icons/Invisible.gif", "resources/icons/Invisible.gif"));
        return map;
    }

}

package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;

/**
 * ItemPanel for for PortalIcons.
 * 
* @author Pete Cressman Copyright (c) 2013, 2020
 */
public class PortalItemPanel extends FamilyItemPanel {

    public PortalItemPanel(DisplayFrame parentFrame, String type, String family) {
        super(parentFrame, type, family);
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _suppressDragging = true;
            _previewPanel.setVisible(false);
            _previewPanel.invalidate();
        }
    }

    @Override
    protected String getDisplayKey() {
        return "toArrow";
    }

}
